package com.wikiknowledge.document;

import com.wikiknowledge.document.dto.DocumentResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.List;/** 文档上传、查询、删除接口 */


@RestController
public class DocumentController {

    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @PostMapping("/api/knowledge-bases/{knowledgeBaseId}/documents")
    public ResponseEntity<DocumentResponse> upload(@PathVariable Long knowledgeBaseId,
                                                   @RequestParam("file") MultipartFile file,
                                                   Authentication authentication)
            throws IOException, NoSuchAlgorithmException {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(documentService.upload(knowledgeBaseId, file, authentication.getName()));
    }

    @GetMapping("/api/knowledge-bases/{knowledgeBaseId}/documents")
    public ResponseEntity<List<DocumentResponse>> list(@PathVariable Long knowledgeBaseId) {
        return ResponseEntity.ok(documentService.list(knowledgeBaseId));
    }

    @GetMapping("/api/documents/{id}")
    public ResponseEntity<DocumentResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(documentService.get(id));
    }

    @DeleteMapping("/api/documents/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, Authentication authentication) throws IOException {
        documentService.delete(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }
}
