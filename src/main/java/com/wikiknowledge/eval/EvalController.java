package com.wikiknowledge.eval;

import com.wikiknowledge.eval.dto.EvalRunRequest;
import com.wikiknowledge.eval.dto.EvalRunResponse;
import com.wikiknowledge.eval.dto.EvalSetCreateRequest;
import com.wikiknowledge.eval.dto.EvalSetDetailResponse;
import com.wikiknowledge.eval.dto.EvalSetResponse;
import com.wikiknowledge.repository.EvalRunRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/evals")
@PreAuthorize("hasRole('ADMIN')")
public class EvalController {

    private final EvalSetService evalSetService;
    private final EvalRunner evalRunner;
    private final EvalRunRepository evalRunRepository;

    public EvalController(EvalSetService evalSetService,
                          EvalRunner evalRunner,
                          EvalRunRepository evalRunRepository) {
        this.evalSetService = evalSetService;
        this.evalRunner = evalRunner;
        this.evalRunRepository = evalRunRepository;
    }

    @GetMapping("/sets")
    public ResponseEntity<List<EvalSetResponse>> listSets() {
        return ResponseEntity.ok(evalSetService.list());
    }

    @PostMapping("/sets")
    public ResponseEntity<EvalSetResponse> createSet(@Valid @RequestBody EvalSetCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(evalSetService.create(request));
    }

    @GetMapping("/sets/{id}")
    public ResponseEntity<EvalSetDetailResponse> getSet(@PathVariable Long id) {
        return ResponseEntity.ok(evalSetService.get(id));
    }

    @DeleteMapping("/sets/{id}")
    public ResponseEntity<Void> deleteSet(@PathVariable Long id) {
        evalSetService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/runs")
    public ResponseEntity<EvalRunResponse> run(@Valid @RequestBody EvalRunRequest request) {
        return ResponseEntity.accepted().body(EvalRunResponse.from(evalRunner.run(request)));
    }

    @GetMapping("/runs/{id}")
    public ResponseEntity<EvalRunResponse> getRun(@PathVariable Long id) {
        return evalRunRepository.findById(id)
                .map(run -> ResponseEntity.ok(EvalRunResponse.from(run)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
