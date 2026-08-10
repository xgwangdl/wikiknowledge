package com.wikiknowledge.rag;

import com.wikiknowledge.rag.dto.ChatRequest;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
public class RagController {

    private final ChatService chatService;

    public RagController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping(value = "/api/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<RagEvent>> chat(@Valid @RequestBody ChatRequest request,
                                                Authentication authentication) {
        return chatService.chat(request, authentication.getName());
    }
}
