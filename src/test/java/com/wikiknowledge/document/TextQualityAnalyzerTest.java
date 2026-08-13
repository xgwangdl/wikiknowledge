package com.wikiknowledge.document;

import com.wikiknowledge.document.extract.TextQualityAnalyzer;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TextQualityAnalyzerTest {

    @Test
    void normalChineseTextIsAccepted() {
        assertThat(TextQualityAnalyzer.isLikelyGarbled(
                "Java 基础知识包括集合、并发、JVM 和 Spring 等内容，适合面试复习。"))
                .isFalse();
    }

    @Test
    void normalEnglishTextIsAccepted() {
        assertThat(TextQualityAnalyzer.isLikelyGarbled(
                "Spring Boot makes it easy to create stand-alone production-grade applications."))
                .isFalse();
    }

    @Test
    void replacementCharactersAreRejected() {
        String garbage = "x".repeat(20) + "\uFFFD".repeat(30);
        assertThat(TextQualityAnalyzer.isLikelyGarbled(garbage)).isTrue();
    }

    @Test
    void mojibakeIsRejected() {
        String mojibake = "\u00E6\u00B5\u008B\u00E8\u00AF\u0095".repeat(10);
        assertThat(TextQualityAnalyzer.isLikelyGarbled(mojibake)).isTrue();
    }
}
