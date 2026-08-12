package com.wikiknowledge.rag;

import com.wikiknowledge.ai.EmbeddingService;
import com.wikiknowledge.domain.KnowledgeBase;
import com.wikiknowledge.exception.BusinessException;
import com.wikiknowledge.rag.dto.ChatRequest;
import com.wikiknowledge.repository.ChunkMatch;
import com.wikiknowledge.repository.ChunkRepository;
import com.wikiknowledge.repository.KnowledgeBaseRepository;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;/** RAG 检索与流式生成服务 */


@Service
public class RagService {

    private static final int TOP_K = 5;
    private static final double MIN_SIMILARITY = 0.3;

    private final KnowledgeBaseRepository knowledgeBaseRepository;
    private final ChunkRepository chunkRepository;
    private final EmbeddingService embeddingService;
    private final ObjectProvider<ChatModel> chatModelProvider;

    public RagService(KnowledgeBaseRepository knowledgeBaseRepository,
                      ChunkRepository chunkRepository,
                      EmbeddingService embeddingService,
                      ObjectProvider<ChatModel> chatModelProvider) {
        this.knowledgeBaseRepository = knowledgeBaseRepository;
        this.chunkRepository = chunkRepository;
        this.embeddingService = embeddingService;
        this.chatModelProvider = chatModelProvider;
    }

    /**
     * 执行 RAG 问答：向量化问题 -> 检索相似切片 -> 组装 Prompt -> 流式回答。
     * 无相关上下文或 AI 未配置时返回 error 事件。
     */
    public Flux<ServerSentEvent<RagEvent>> chat(ChatRequest request) {
        // 1. 校验知识库是否存在
        KnowledgeBase knowledgeBase = knowledgeBaseRepository.findById(request.knowledgeBaseId())
                .orElseThrow(() -> new BusinessException("KNOWLEDGE_BASE_NOT_FOUND", "知识库不存在"));

        // 2. 将用户问题向量化
        float[] queryVector = embeddingService.embed(request.question());
        // 3. 检索相似切片，并过滤相似度过低的结果
        List<ChunkMatch> matches = chunkRepository.searchSimilar(
                        knowledgeBase.getId(),
                        embeddingService.toVectorLiteral(queryVector),
                        TOP_K
                ).stream()
                .filter(match -> match.getSimilarity() != null && match.getSimilarity() >= MIN_SIMILARITY)
                .toList();

        if (matches.isEmpty()) {
            return Flux.just(serverEvent(new RagEvent(
                    "error",
                    Map.of("code", "NO_CONTEXT", "message", "知识库中未找到相关资料")
            )));
        }

        ChatModel chatModel = chatModelProvider.getIfAvailable();
        if (chatModel == null) {
            return Flux.just(serverEvent(new RagEvent(
                    "error",
                    Map.of("code", "AI_NOT_ENABLED", "message", "AI 服务未配置")
            )));
        }

        // 4. 组装 Prompt 后流式调用大模型
        String prompt = buildPrompt(matches, request.question());
        Flux<ChatResponse> responses = chatModel.stream(new Prompt(prompt));
        List<Map<String, Object>> citations = buildCitations(matches);

        return Flux.concat(
                Flux.just(serverEvent(new RagEvent(
                        "start",
                        Map.of("knowledgeBaseId", knowledgeBase.getId())
                ))),
                responses.map(response -> {
                    String content = response.getResult().getOutput().getText();
                    return serverEvent(new RagEvent(
                            "delta",
                            Map.of("content", content == null ? "" : content)
                    ));
                }),
                Flux.just(serverEvent(new RagEvent(
                        "done",
                        Map.of("citations", citations)
                )))
        );
    }

    /**
     * 组装 RAG Prompt，约束模型只依据知识库资料回答。
     */
    private String buildPrompt(List<ChunkMatch> matches, String question) {
        StringBuilder sb = new StringBuilder("以下是知识库中的相关资料：\n\n");
        int index = 1;
        for (ChunkMatch match : matches) {
            sb.append("【资料").append(index++).append("】\n");
            sb.append(match.getContent()).append("\n\n");
        }
        sb.append("请仅根据以上资料回答用户问题，不要编造资料中没有的内容。");
        sb.append("如果资料不足，请明确说明。\n\n用户问题：").append(question);
        return sb.toString();
    }

    /**
     * 将命中的切片转换为引用来源列表。
     */
    private List<Map<String, Object>> buildCitations(List<ChunkMatch> matches) {
        List<Map<String, Object>> citations = new ArrayList<>();
        for (ChunkMatch match : matches) {
            citations.add(Map.of(
                    "chunkId", match.getId(),
                    "documentId", match.getDocumentId(),
                    "seqNo", match.getSeqNo(),
                    "similarity", match.getSimilarity()
            ));
        }
        return citations;
    }

    private ServerSentEvent<RagEvent> serverEvent(RagEvent event) {
        return ServerSentEvent.builder(event).build();
    }
}
