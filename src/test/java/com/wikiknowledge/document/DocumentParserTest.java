package com.wikiknowledge.document;

import com.wikiknowledge.ai.PdfOcrService;
import com.wikiknowledge.ai.VectorizationService;
import com.wikiknowledge.document.extract.DocumentTextExtractor;
import com.wikiknowledge.document.storage.LocalFileStorage;
import com.wikiknowledge.domain.Chunk;
import com.wikiknowledge.domain.Document;
import com.wikiknowledge.repository.ChunkRepository;
import com.wikiknowledge.repository.DocumentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DocumentParserTest {

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private ChunkRepository chunkRepository;

    @Mock
    private DocumentTextExtractor textExtractor;

    @Mock
    private TextChunker textChunker;

    @Mock
    private LocalFileStorage fileStorage;

    @Mock
    private VectorizationService vectorizationService;

    @Mock
    private PdfOcrService pdfOcrService;

    @InjectMocks
    private DocumentParser documentParser;

    @Test
    void garbledPdfFallsBackToOcr() throws Exception {
        Document document = document(8L, "Java基础知识篇.pdf");
        when(documentRepository.findById(8L)).thenReturn(Optional.of(document));
        when(fileStorage.read(8L, "Java基础知识篇.pdf"))
                .thenReturn(new ByteArrayInputStream(new byte[]{1}));
        when(textExtractor.extract(any(InputStream.class), eq("Java基础知识篇.pdf")))
                .thenReturn("\uFFFD".repeat(50));
        when(pdfOcrService.extractText(any(InputStream.class)))
                .thenReturn("Java 基础知识点：集合、并发、JVM、Spring 等内容。");
        when(textChunker.chunk(anyString())).thenReturn(
                List.of("Java 基础知识点：集合、并发、JVM、Spring 等内容。"));

        documentParser.parseAsync(8L);

        verify(pdfOcrService).extractText(any(InputStream.class));
        verify(chunkRepository).save(any(Chunk.class));
        assertThat(document.getStatus()).isEqualTo("READY");
    }

    @Test
    void garbledPdfFailsWhenOcrUnavailable() throws Exception {
        Document document = document(8L, "Java基础知识篇.pdf");
        when(documentRepository.findById(8L)).thenReturn(Optional.of(document));
        when(fileStorage.read(8L, "Java基础知识篇.pdf"))
                .thenReturn(new ByteArrayInputStream(new byte[]{1}));
        when(textExtractor.extract(any(InputStream.class), eq("Java基础知识篇.pdf")))
                .thenReturn("\uFFFD".repeat(50));
        when(pdfOcrService.extractText(any(InputStream.class))).thenReturn(null);

        documentParser.parseAsync(8L);

        verify(chunkRepository, never()).save(any());
        assertThat(document.getStatus()).isEqualTo("FAILED");
        assertThat(document.getErrorMessage()).contains("乱码");
    }

    private Document document(Long id, String filename) {
        Document document = new Document();
        document.setId(id);
        document.setFilename(filename);
        document.setKnowledgeBaseId(1L);
        document.setStatus("UPLOADED");
        return document;
    }
}
