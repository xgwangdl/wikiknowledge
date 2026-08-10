package com.wikiknowledge.session;

import com.wikiknowledge.session.dto.CreateSessionRequest;
import com.wikiknowledge.session.dto.SessionDetailResponse;
import com.wikiknowledge.session.dto.SessionResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/sessions")
public class SessionController {

    private final SessionService sessionService;

    public SessionController(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @GetMapping
    public ResponseEntity<List<SessionResponse>> list(Authentication authentication) {
        return ResponseEntity.ok(sessionService.list(authentication.getName()));
    }

    @PostMapping
    public ResponseEntity<SessionResponse> create(@Valid @RequestBody CreateSessionRequest request,
                                                  Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(SessionResponse.from(sessionService.createSession(request, authentication.getName())));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SessionDetailResponse> get(@PathVariable Long id, Authentication authentication) {
        return ResponseEntity.ok(sessionService.get(id, authentication.getName()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, Authentication authentication) {
        sessionService.delete(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }
}
