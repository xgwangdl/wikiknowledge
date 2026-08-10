package com.wikiknowledge.rag;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wikiknowledge.domain.Message;
import com.wikiknowledge.domain.Session;
import com.wikiknowledge.rag.dto.ChatRequest;
import com.wikiknowledge.repository.MessageRepository;
import com.wikiknowledge.session.SessionService;
import com.wikiknowledge.session.dto.CreateSessionRequest;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class ChatService {

    private final RagService ragService;
    private final SessionService sessionService;
    private final MessageRepository messageRepository;
    private final ObjectMapper objectMapper;
    private final PromptGuardService promptGuardService;

    public ChatService(RagService ragService,
                       SessionService sessionService,
                       MessageRepository messageRepository,
                       ObjectMapper objectMapper,
                       PromptGuardService promptGuardService) {
        this.ragService = ragService;
        this.sessionService = sessionService;
        this.messageRepository = messageRepository;
        this.objectMapper = objectMapper;
        this.promptGuardService = promptGuardService;
    }

    public Flux<ServerSentEvent<RagEvent>> chat(ChatRequest request, String username) {
        promptGuardService.validate(request.question());
        Session session;
        if (request.sessionId() == null) {
            session = sessionService.createSession(
                    new CreateSessionRequest(request.knowledgeBaseId(), request.title()),
                    username
            );
        } else {
            session = sessionService.getOwnedSession(request.sessionId(), username);
        }

        saveMessage(session.getId(), "user", request.question(), null);
        AtomicReference<StringBuilder> answerRef = new AtomicReference<>(new StringBuilder());
        AtomicReference<String> citationsRef = new AtomicReference<>("[]");

        return ragService.chat(request)
                .doOnNext(serverEvent -> collect(serverEvent.data(), answerRef, citationsRef))
                .doOnComplete(() -> saveMessage(
                        session.getId(),
                        "assistant",
                        answerRef.get().toString(),
                        citationsRef.get()
                ))
                .map(serverEvent -> decorateStart(serverEvent, session.getId()));
    }

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
                    // keep default empty JSON array
                }
            }
        }
    }

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
