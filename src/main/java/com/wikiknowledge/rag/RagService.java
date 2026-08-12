package com.wikiknowledge.rag;

import com.wikiknowledge.domain.KnowledgeBase;
import com.wikiknowledge.exception.BusinessException;
import com.wikiknowledge.rag.dto.ChatHistory;
import com.wikiknowledge.rag.dto.ChatRequest;
import com.wikiknowledge.repository.ChunkMatch;
import com.wikiknowledge.repository.KnowledgeBaseRepository;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** RAG 检索与流式生成服务。 */
@Service
public class RagService {

    private static final int TOP_K = 5;
    private static final double MIN_SIMILARITY = 0.3;
    private static final Duration RESPONSE_TIMEOUT = Duration.ofSeconds(30);

    private final KnowledgeBaseRepository knowledgeBaseRepository;
    private final HybridSearchService hybridSearchService;
    private final ObjectProvider<ChatModel> chatModelProvider;

    public RagService(KnowledgeBaseRepository knowledgeBaseRepository,
                      HybridSearchService hybridSearchService,
                      ObjectProvider<ChatModel> chatModelProvider) {
        this.knowledgeBaseRepository = knowledgeBaseRepository;
        this.hybridSearchService = hybridSearchService;
        this.chatModelProvider = chatModelProvider;
    }

    /**
     * 执行 RAG 问答（无历史）。
     *
     * @param request 聊天请求
     * @return SSE 事件流
     */
    public Flux<ServerSentEvent<RagEvent>> chat(ChatRequest request) {
        return chat(request, List.of());
    }

    /**
     * 执行 RAG 问答：混合检索 -> 组装 Prompt（含历史） -> 流式回答。
     * 回答带 30 秒超时、一次自动重试与失败降级。
     *
     * @param request 聊天请求
     * @param history 最近的对话历史
     * @return SSE 事件流
     */
    public Flux<ServerSentEvent<RagEvent>> chat(ChatRequest request, List<ChatHistory> history) {
        KnowledgeBase knowledgeBase = knowledgeBaseRepository.findById(request.knowledgeBaseId())
                .orElseThrow(() -> new BusinessException("KNOWLEDGE_BASE_NOT_FOUND", "知识库不存在"));

        List<ChunkMatch> matches = hybridSearchService.search(
                        knowledgeBase.getId(),
                        request.question(),
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

        String prompt = buildPrompt(matches, request.question(), history);
        Flux<ServerSentEvent<RagEvent>> answerEvents = chatModel.stream(new Prompt(prompt))
                .timeout(RESPONSE_TIMEOUT)
                .retryWhen(Retry.backoff(1, Duration.ofMillis(500)))
                .map(response -> serverEvent(new RagEvent(
                        "delta",
                        Map.of("content", safeText(response))
                )))
                .onErrorResume(ex -> Flux.just(serverEvent(new RagEvent(
                        "error",
                        Map.of("code", "AI_RESPONSE_ERROR", "message", "回答超时或失败，请稍后重试")
                ))));

        List<Map<String, Object>> citations = buildCitations(matches);

        return Flux.concat(
                Flux.just(serverEvent(new RagEvent(
                        "start",
                        Map.of("knowledgeBaseId", knowledgeBase.getId())
                ))),
                answerEvents,
                Flux.just(serverEvent(new RagEvent(
                        "done",
                        Map.of("citations", citations)
                )))
        );
    }

    private String safeText(ChatResponse response) {
        String text = response.getResult().getOutput().getText();
        return text == null ? "" : text;
    }

    /**
     * 组装 RAG Prompt：资料 + 对话历史 + 当前问题。
     *
     * @param matches  命中的知识库切片
     * @param question 用户问题
     * @param history  最近的对话历史
     * @return Prompt 文本
     */
    private String buildPrompt(List<ChunkMatch> matches, String question, List<ChatHistory> history) {
        StringBuilder sb = new StringBuilder("以下是知识库中的相关资料：\n\n");
        int index = 1;
        for (ChunkMatch match : matches) {
            sb.append("【资料").append(index++).append("】\n");
            sb.append(match.getContent()).append("\n\n");
        }
        if (history != null && !history.isEmpty()) {
            sb.append("对话历史：\n");
            for (ChatHistory item : history) {
                sb.append(item.role()).append(": ").append(item.content()).append("\n");
            }
            sb.append("\n");
        }
        sb.append("请仅根据以上资料回答用户问题，不要编造资料中没有的内容。");
        sb.append("如果资料不足，请明确说明。\n\n用户问题：").append(question);
        return sb.toString();
    }

    /**
     * 将命中的切片转换为引用来源列表。
     *
     * @param matches 命中的知识库切片
     * @return 引用来源列表
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
