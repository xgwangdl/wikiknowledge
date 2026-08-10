package com.wikiknowledge.document;

import com.wikiknowledge.document.extract.TikaTextExtractor;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class TikaTextExtractorTest {

    private final TikaTextExtractor extractor = new TikaTextExtractor();

    @Test
    void extractsPlainText() throws Exception {
        String content = "维基知识库测试文档";
        String result = extractor.extract(
                new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)),
                "sample.txt"
        );
        assertThat(result).contains("维基知识库");
    }
}
