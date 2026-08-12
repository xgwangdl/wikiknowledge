package com.wikiknowledge.rag;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wikiknowledge.common.AICostGuardService;
import com.wikiknowledge.domain.Message;
import com.wikiknowledge.domain.Session;
import com.wikiknowledge.rag.dto.ChatHistory;
import com.wikiknowledge.rag.dto.ChatRequest;
import com.wikiknowledge.repository.MessageRepository;
import com.wikiknowledge.session.SessionService;
import com.wikiknowledge.session.dto.CreateSessionRequest;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/** 聊天编排服务：成本控制、会话创建、历史上下文、消息持久化与 SSE 转发。 */
@Service
public class ChatService {

    private final RagService ragService;
    private final SessionService sessionService;
    private final MessageRepository messageRepository;
    private final ObjectMapper objectMapper;
    private final PromptGuardService promptGuardService;
    private final AICostGuardService aiCostGuardService;

    public ChatService(RagService ragService,
                       SessionService sessionService,
                       MessageRepository messageRepository,
                       ObjectMapper objectMapper,
                       PromptGuardService promptGuardService,
                       AICostGuardService aiCostGuardService) {
        this.ragService = ragService;
        this.sessionService = sessionService;
        this.messageRepository = messageRepository;
        this.objectMapper = objectMapper;
        this.promptGuardService = promptGuardService;
        this.aiCostGuardService = aiCostGuardService;
    }

    /**
     * 编排一次聊天：校验提示与成本 -> 创建/复用会话 -> 携带历史调用 RAG -> 持久化消息。
     *
     * @param request  聊天请求
     * @param username 当前登录用户名
     * @return SSE 事件流
     */
    public Flux<ServerSentEvent<RagEvent>> chat(ChatRequest request, String username) {
        promptGuardService.validate(request.question());
        aiCostGuardService.check(username, request.question());

        Session session;
        if (request.sessionId() == null) {
            session = sessionService.createSession(
                    new CreateSessionRequest(request.knowledgeBaseId(), request.title()),
                    username
            );
        } else {
            session = sessionService.getOwnedSession(request.sessionId(), username);
        }

        List<ChatHistory> history = loadHistory(session);
        saveMessage(session.getId(), "user", request.question(), null);

        AtomicReference<StringBuilder> answerRef = new AtomicReference<>(new StringBuilder());
        AtomicReference<String> citationsRef = new AtomicReference<>("[]");

        return ragService.chat(request, history)
                .doOnNext(serverEvent -> collect(serverEvent.data(), answerRef, citationsRef))
                .doOnComplete(() -> saveMessage(
                        session.getId(),
                        "assistant",
                        answerRef.get().toString(),
                        citationsRef.get()
                ))
                .map(serverEvent -> decorateStart(serverEvent, session.getId()));
    }

    /**
     * 加载最近 10 条会话历史作为 RAG 多轮上下文。
     *
     * @param session 当前会话
     * @return 历史消息列表
     */
    private List<ChatHistory> loadHistory(Session session) {
        if (session.getId() == null) {
            return List.of();
        }
        List<Message> messages = messageRepository.findBySessionIdOrderByIdAsc(session.getId());
        int from = Math.max(0, messages.size() - 10);
        return messages.subList(from, messages.size()).stream()
                .map(message -> new ChatHistory(message.getRole(), message.getContent()))
                .toList();
    }

    /**
     * 从 SSE 事件中累积回答文本与引用来源。
     *
     * @param event        当前 SSE 事件
     * @param answerRef    回答文本累积器
     * @param citationsRef 引用来源 JSON 累积器
     */
    private void collect(RagEvent event,
                         AtomicReference<StringBuilder> answerRef,
                         AtomicReference<String> citationsRef) {
        if (event == null || event.data() == null) {
            return;
        }
        if ("delta".equals(event.type())) {
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) event.data();
            Object content = data.get("content");
            if (content != null) {
                answerRef.get().append(content);
            }
        } else if ("done".equals(event.type())) {
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) event.data();
            Object citations = data.get("citations");
            if (citations != null) {
                try {
                    citationsRef.set(objectMapper.writeValueAsString(citations));
                } catch (JsonProcessingException ignored) {
                    // 保持默认空数组
                }
            }
        }
    }

    /**
     * 在 start 事件中补充 sessionId。
     *
     * @param serverEvent 原始 SSE 事件
     * @param sessionId   当前会话 ID
     * @return 补充后的 SSE 事件
     */
    private ServerSentEvent<RagEvent> decorateStart(ServerSentEvent<RagEvent> serverEvent, Long sessionId) {
        RagEvent event = serverEvent.data();
        if (event == null || !"start".equals(event.type()) || !(event.data() instanceof Map<?, ?>)) {
            return serverEvent;
        }
        Map<String, Object> data = new HashMap<>((Map<String, Object>) event.data());
        data.put("sessionId", sessionId);
        return ServerSentEvent.builder(new RagEvent("start", data)).build();
    }

    private void saveMessage(Long sessionId, String role, String content, String citations) {
        Message message = new Message();
        message.setSessionId(sessionId);
        message.setRole(role);
        message.setContent(content);
        message.setCitations(citations);
        messageRepository.save(message);
    }
}
