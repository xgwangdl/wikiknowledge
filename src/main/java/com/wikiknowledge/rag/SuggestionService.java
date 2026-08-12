package com.wikiknowledge.rag;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wikiknowledge.ai.EmbeddingService;
import com.wikiknowledge.domain.Chunk;
import com.wikiknowledge.exception.BusinessException;
import com.wikiknowledge.repository.ChunkMatch;
import com.wikiknowledge.repository.ChunkRepository;
import com.wikiknowledge.repository.KnowledgeBaseRepository;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;/** 问题建议生成服务 */


@Service
public class SuggestionService {

    private static final String CACHE_PREFIX = "suggest:";
    private static final Duration CACHE_TTL = Duration.ofMinutes(10);

    private final KnowledgeBaseRepository knowledgeBaseRepository;
    private final ChunkRepository chunkRepository;
    private final EmbeddingService embeddingService;
    private final ObjectProvider<ChatModel> chatModelProvider;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public SuggestionService(KnowledgeBaseRepository knowledgeBaseRepository,
                             ChunkRepository chunkRepository,
                             EmbeddingService embeddingService,
                             ObjectProvider<ChatModel> chatModelProvider,
                             StringRedisTemplate redisTemplate,
                             ObjectMapper objectMapper) {
        this.knowledgeBaseRepository = knowledgeBaseRepository;
        this.chunkRepository = chunkRepository;
        this.embeddingService = embeddingService;
        this.chatModelProvider = chatModelProvider;
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * 生成问题建议：先查 Redis 缓存，未命中则基于知识库内容生成并缓存。
     */
    public List<String> suggest(Long knowledgeBaseId, String query) {
        knowledgeBaseRepository.findById(knowledgeBaseId)
                .orElseThrow(() -> new BusinessException("KNOWLEDGE_BASE_NOT_FOUND", "知识库不存在"));

        // 1. 尝试读取缓存
        String cacheKey = CACHE_PREFIX + knowledgeBaseId + ":" + (query == null ? "" : query);
        String cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            try {
                return objectMapper.readValue(cached, new TypeReference<List<String>>() {
                });
            } catch (Exception ignored) {
                // 缓存解析失败时重新生成
            }
        }

        // 2. 未命中缓存时生成建议并写入缓存
        List<String> suggestions = buildSuggestions(knowledgeBaseId, query);
        try {
            redisTemplate.opsForValue().set(cacheKey, objectMapper.writeValueAsString(suggestions), CACHE_TTL);
        } catch (Exception ignored) {
            // 缓存失败不影响主流程
        }
        return suggestions;
    }

    /**
     * 生成建议：优先使用 LLM，AI 不可用时回退为基于切片内容的模板问题。
     */
    private List<String> buildSuggestions(Long knowledgeBaseId, String query) {
        List<String> contents = loadChunkContents(knowledgeBaseId, query);
        if (contents.isEmpty()) {
            return defaultSuggestions();
        }

        ChatModel chatModel = chatModelProvider.getIfAvailable();
        if (chatModel == null) {
            return fallbackSuggestions(contents);
        }

        String prompt = buildPrompt(contents);
        String content = chatModel.call(new Prompt(prompt)).getResult().getOutput().getText();
        try {
            JsonNode node = objectMapper.readTree(content);
            if (node.isArray()) {
                List<String> questions = new ArrayList<>();
                node.forEach(item -> questions.add(item.asText()));
                if (!questions.isEmpty()) {
                    return questions.stream().limit(3).toList();
                }
            }
        } catch (Exception ignored) {
            // 模型返回格式异常时使用兜底
        }
        return fallbackSuggestions(contents);
    }

    /**
     * 加载用于生成建议的知识库片段；有查询词时走向量检索，否则取前 3 个切片。
     */
    private List<String> loadChunkContents(Long knowledgeBaseId, String query) {
        if (query == null || query.isBlank()) {
            return chunkRepository.findTop3ByKnowledgeBaseIdOrderByIdAsc(knowledgeBaseId).stream()
                    .map(Chunk::getContent)
                    .toList();
        }
        float[] vector = embeddingService.embed(query);
        List<ChunkMatch> matches = chunkRepository.searchSimilar(
                knowledgeBaseId,
                embeddingService.toVectorLiteral(vector),
                3
        );
        return matches.stream().map(ChunkMatch::getContent).toList();
    }

    private String buildPrompt(List<String> contents) {
        StringBuilder sb = new StringBuilder("请根据以下知识库片段，生成 3 个用户可能会问的问题。");
        sb.append("问题要具体、简洁，只返回 JSON 数组，例如 [\"问题1\",\"问题2\",\"问题3\"]。\n\n");
        for (int i = 0; i < contents.size(); i++) {
            sb.append(i + 1).append(". ").append(contents.get(i)).append("\n");
        }
        return sb.toString();
    }

    private List<String> fallbackSuggestions(List<String> contents) {
        List<String> suggestions = new ArrayList<>();
        for (int i = 0; i < Math.min(3, contents.size()); i++) {
            String snippet = contents.get(i).replaceAll("\\s+", " ").trim();
            if (snippet.length() > 60) {
                snippet = snippet.substring(0, 60);
            }
            suggestions.add("关于「" + snippet + "」可以问什么？");
        }
        return suggestions;
    }

    private List<String> defaultSuggestions() {
        return List.of(
                "这个知识库里包含哪些内容？",
                "如何快速找到我需要的资料？",
                "有哪些使用示例可以了解？"
        );
    }
}
