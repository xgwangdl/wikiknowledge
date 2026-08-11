package com.wikiknowledge.rag;

import com.wikiknowledge.rag.dto.SuggestionResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/knowledge-bases")
public class SuggestionController {

    private final SuggestionService suggestionService;

    public SuggestionController(SuggestionService suggestionService) {
        this.suggestionService = suggestionService;
    }

    @GetMapping("/{id}/suggestions")
    public ResponseEntity<SuggestionResponse> suggest(@PathVariable Long id,
                                                       @RequestParam(required = false) String query) {
        return ResponseEntity.ok(new SuggestionResponse(suggestionService.suggest(id, query)));
    }
}
