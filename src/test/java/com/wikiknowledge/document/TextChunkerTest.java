package com.wikiknowledge.document;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TextChunkerTest {

    private final TextChunker textChunker = new TextChunker();

    @Test
    void keepsShortParagraphAsSingleChunk() {
        List<String> chunks = textChunker.chunk("第一段内容\n\n第二段内容");
        assertThat(chunks).hasSize(2);
    }

    @Test
    void splitsLongParagraphWithOverlap() {
        String longText = "维基知识库".repeat(200);
        List<String> chunks = textChunker.chunk(longText);
        assertThat(chunks.size()).isGreaterThan(1);
        assertThat(chunks.get(0)).hasSizeLessThanOrEqualTo(TextChunker.CHUNK_SIZE);
    }
}
