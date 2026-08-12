package com.wikiknowledge.rag;

import com.wikiknowledge.exception.BusinessException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;/** 提示注入防护服务 */


@Service
public class PromptGuardService {

    private static final int MAX_LENGTH = 2000;
    private static final List<String> INJECTION_PATTERNS = List.of(
            "ignore previous instructions",
            "ignore all previous",
            "忽略之前的指令",
            "忽略所有指令",
            "system prompt"
    );

    public void validate(String question) {
        if (question == null || question.isBlank()) {
            throw new BusinessException("EMPTY_QUESTION", "问题不能为空");
        }
        if (question.length() > MAX_LENGTH) {
            throw new BusinessException("QUESTION_TOO_LONG", "问题长度不能超过 2000 字符");
        }
        String lower = question.toLowerCase(Locale.ROOT);
        for (String pattern : INJECTION_PATTERNS) {
            if (lower.contains(pattern)) {
                throw new BusinessException("PROMPT_INJECTION", "问题包含不允许的内容");
            }
        }
    }
}
