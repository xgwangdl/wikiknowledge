package com.wikiknowledge.document;

import com.wikiknowledge.domain.Chunk;
import com.wikiknowledge.domain.Document;
import com.wikiknowledge.document.extract.DocumentTextExtractor;
import com.wikiknowledge.document.extract.TextQualityAnalyzer;
import com.wikiknowledge.document.storage.LocalFileStorage;
import com.wikiknowledge.ai.VectorizationService;
import com.wikiknowledge.repository.ChunkRepository;
import com.wikiknowledge.repository.DocumentRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;/** 文档异步解析、切片与向量化 */


@Service
public class DocumentParser {

    private final DocumentRepository documentRepository;
    private final ChunkRepository chunkRepository;
    private final DocumentTextExtractor textExtractor;
    private final TextChunker textChunker;
    private final LocalFileStorage fileStorage;
    private final VectorizationService vectorizationService;

    public DocumentParser(DocumentRepository documentRepository,
                          ChunkRepository chunkRepository,
                          DocumentTextExtractor textExtractor,
                          TextChunker textChunker,
                          LocalFileStorage fileStorage,
                          VectorizationService vectorizationService) {
        this.documentRepository = documentRepository;
        this.chunkRepository = chunkRepository;
        this.textExtractor = textExtractor;
        this.textChunker = textChunker;
        this.fileStorage = fileStorage;
        this.vectorizationService = vectorizationService;
    }

    /**
     * 异步解析文档：提取文本 -> 切片 -> 向量化 -> 更新文档状态。
     * 任一步骤失败都会把文档标记为 FAILED。
     *
     * @param documentId 要解析的文档 ID
     */
    @Async
    public void parseAsync(Long documentId) {
        Document document = documentRepository.findById(documentId).orElse(null);
        if (document == null) {
            return;
        }
        // 1. 先更新为解析中状态
        document.setStatus("PARSING");
        documentRepository.save(document);
        try {
            // 2. 读取本地文件并提取文本
            String text;
            try (InputStream inputStream = fileStorage.read(document.getId(), document.getFilename())) {
                text = textExtractor.extract(inputStream, document.getFilename());
            }
            if (text.isBlank()) {
                fail(document, "文档内容为空，无法解析");
                return;
            }
            // 3. 切片并入库
            if (TextQualityAnalyzer.isLikelyGarbled(text)) {
                fail(document, "文档文本提取异常（疑似乱码）：请将 PDF 另存为带文本层的文件，或转换为 Word/Markdown 后重新上传");
                return;
            }
            List<String> chunks = textChunker.chunk(text);
            chunkRepository.deleteByDocumentId(document.getId());
            List<com.wikiknowledge.domain.Chunk> savedChunks = new ArrayList<>();
            int seq = 0;
            for (String content : chunks) {
                Chunk chunk = new Chunk();
                chunk.setDocumentId(document.getId());
                chunk.setKnowledgeBaseId(document.getKnowledgeBaseId());
                chunk.setContent(content);
                chunk.setSeqNo(seq++);
                chunk.setTokenCount(Math.max(1, content.length() / 4));
                savedChunks.add(chunkRepository.save(chunk));
            }
            // 4. 为每个切片生成向量
            vectorizationService.vectorize(savedChunks);
            // 5. 更新文档为 READY
            document.setChunkCount(chunks.size());
            document.setStatus("READY");
            document.setErrorMessage(null);
            documentRepository.save(document);
        } catch (Exception ex) {
            fail(document, ex.getMessage() == null ? "解析失败" : ex.getMessage());
        }
    }

    private void fail(Document document, String message) {
        document.setStatus("FAILED");
        document.setErrorMessage(message.length() > 500 ? message.substring(0, 500) : message);
        documentRepository.save(document);
    }
}
