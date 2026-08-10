package com.wikiknowledge.document;

import com.wikiknowledge.domain.Chunk;
import com.wikiknowledge.domain.Document;
import com.wikiknowledge.document.extract.DocumentTextExtractor;
import com.wikiknowledge.document.storage.LocalFileStorage;
import com.wikiknowledge.repository.ChunkRepository;
import com.wikiknowledge.repository.DocumentRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.List;

@Service
public class DocumentParser {

    private final DocumentRepository documentRepository;
    private final ChunkRepository chunkRepository;
    private final DocumentTextExtractor textExtractor;
    private final TextChunker textChunker;
    private final LocalFileStorage fileStorage;

    public DocumentParser(DocumentRepository documentRepository,
                          ChunkRepository chunkRepository,
                          DocumentTextExtractor textExtractor,
                          TextChunker textChunker,
                          LocalFileStorage fileStorage) {
        this.documentRepository = documentRepository;
        this.chunkRepository = chunkRepository;
        this.textExtractor = textExtractor;
        this.textChunker = textChunker;
        this.fileStorage = fileStorage;
    }

    @Async
    public void parseAsync(Long documentId) {
        Document document = documentRepository.findById(documentId).orElse(null);
        if (document == null) {
            return;
        }
        document.setStatus("PARSING");
        documentRepository.save(document);
        try {
            String text;
            try (InputStream inputStream = fileStorage.read(document.getId(), document.getFilename())) {
                text = textExtractor.extract(inputStream, document.getFilename());
            }
            if (text.isBlank()) {
                fail(document, "文档内容为空，无法解析");
                return;
            }
            List<String> chunks = textChunker.chunk(text);
            chunkRepository.deleteByDocumentId(document.getId());
            int seq = 0;
            for (String content : chunks) {
                Chunk chunk = new Chunk();
                chunk.setDocumentId(document.getId());
                chunk.setKnowledgeBaseId(document.getKnowledgeBaseId());
                chunk.setContent(content);
                chunk.setSeqNo(seq++);
                chunk.setTokenCount(Math.max(1, content.length() / 4));
                chunkRepository.save(chunk);
            }
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
