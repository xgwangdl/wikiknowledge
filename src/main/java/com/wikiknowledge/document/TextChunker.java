package com.wikiknowledge.document;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;/** 文本切片器 */


@Component
public class TextChunker {

    public static final int CHUNK_SIZE = 500;
    public static final int OVERLAP = 50;

    /**
     * 文本切片：先按空行分段落，段落过长再按固定长度切片并保留重叠。
     */
    public List<String> chunk(String text) {
        List<String> result = new ArrayList<>();
        String normalized = text.replace("\r\n", "\n");
        for (String paragraph : normalized.split("\\n\\s*\\n")) {
            String cleaned = paragraph.trim();
            if (cleaned.isEmpty()) {
                continue;
            }
            if (cleaned.length() <= CHUNK_SIZE) {
                result.add(cleaned);
                continue;
            }
            int start = 0;
            while (start < cleaned.length()) {
                int end = Math.min(start + CHUNK_SIZE, cleaned.length());
                result.add(cleaned.substring(start, end));
                if (end == cleaned.length()) {
                    break;
                }
                start = Math.max(end - OVERLAP, start + 1);
            }
        }
        return result;
    }
}
