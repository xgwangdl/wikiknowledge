package com.wikiknowledge.rag;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wikiknowledge.domain.Message;
import com.wikiknowledge.domain.Session;
import com.wikiknowledge.rag.dto.ChatRequest;
import com.wikiknowledge.repository.MessageRepository;
import com.wikiknowledge.session.SessionService;
import com.wikiknowledge.session.dto.CreateSessionRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.codec.ServerSentEvent;
import reactor.core.publisher.Flux;

import org.junit.jupiter.api.BeforeEach;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

    @Mock
    private RagService ragService;

    @Mock
    private SessionService sessionService;

    @Mock
    private MessageRepository messageRepository;

    private ChatService chatService;

    @BeforeEach
    void setUp() {
        chatService = new ChatService(ragService, sessionService, messageRepository, new ObjectMapper());
    }

    @Test
    void chatCreatesSessionAndPersistsMessages() {
        Session session = new Session();
        session.setId(10L);
        session.setUserId(1L);
        when(sessionService.createSession(any(CreateSessionRequest.class), any()))
                .thenReturn(session);
        when(ragService.chat(any(ChatRequest.class))).thenReturn(Flux.just(
                event("start", Map.of("knowledgeBaseId", 1L)),
                event("delta", Map.of("content", "你好")),
                event("done", Map.of("citations", List.of(Map.of("chunkId", 1L))))
        ));

        List<ServerSentEvent<RagEvent>> events = chatService.chat(
                new ChatRequest(1L, "你好", null, "新会话"),
                "alice"
        ).collectList().block();

        assertThat(events).hasSize(3);
        verify(messageRepository, times(2)).save(any(Message.class));
    }

    @Test
    void chatUsesExistingSession() {
        Session session = new Session();
        session.setId(20L);
        session.setUserId(1L);
        when(sessionService.getOwnedSession(20L, "alice")).thenReturn(session);
        when(ragService.chat(any(ChatRequest.class))).thenReturn(Flux.just(
                event("start", Map.of("knowledgeBaseId", 1L)),
                event("done", Map.of("citations", List.of()))
        ));

        chatService.chat(new ChatRequest(1L, "你好", 20L, null), "alice")
                .collectList()
                .block();

        verify(sessionService, never()).createSession(any(CreateSessionRequest.class), any());
    }

    private ServerSentEvent<RagEvent> event(String type, Object data) {
        return ServerSentEvent.builder(new RagEvent(type, data)).build();
    }
}
