package com.wikiknowledge.knowledge;

import com.wikiknowledge.knowledge.dto.KnowledgeBaseCreateRequest;
import com.wikiknowledge.knowledge.dto.KnowledgeBaseResponse;
import com.wikiknowledge.knowledge.dto.KnowledgeBaseUpdateRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/knowledge-bases")
public class KnowledgeBaseController {

    private final KnowledgeBaseService knowledgeBaseService;

    public KnowledgeBaseController(KnowledgeBaseService knowledgeBaseService) {
        this.knowledgeBaseService = knowledgeBaseService;
    }

    @GetMapping
    public ResponseEntity<List<KnowledgeBaseResponse>> list() {
        return ResponseEntity.ok(knowledgeBaseService.list());
    }

    @GetMapping("/{id}")
    public ResponseEntity<KnowledgeBaseResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(knowledgeBaseService.get(id));
    }

    @PostMapping
    public ResponseEntity<KnowledgeBaseResponse> create(@Valid @RequestBody KnowledgeBaseCreateRequest request,
                                                        Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(knowledgeBaseService.create(request, authentication.getName()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<KnowledgeBaseResponse> update(@PathVariable Long id,
                                                        @Valid @RequestBody KnowledgeBaseUpdateRequest request,
                                                        Authentication authentication) {
        return ResponseEntity.ok(knowledgeBaseService.update(id, request, authentication.getName()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, Authentication authentication) {
        knowledgeBaseService.delete(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }
}
