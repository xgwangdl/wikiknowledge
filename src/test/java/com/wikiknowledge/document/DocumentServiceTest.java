package com.wikiknowledge.document;

import com.wikiknowledge.domain.Document;
import com.wikiknowledge.domain.KnowledgeBase;
import com.wikiknowledge.domain.User;
import com.wikiknowledge.document.dto.DocumentResponse;
import com.wikiknowledge.document.storage.LocalFileStorage;
import com.wikiknowledge.exception.BusinessException;
import com.wikiknowledge.repository.DocumentRepository;
import com.wikiknowledge.repository.KnowledgeBaseRepository;
import com.wikiknowledge.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DocumentServiceTest {

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private KnowledgeBaseRepository knowledgeBaseRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private LocalFileStorage fileStorage;

    @Mock
    private DocumentParser documentParser;

    @InjectMocks
    private DocumentService documentService;

    @Test
    void uploadRejectsUnsupportedExtension() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "virus.exe", "application/octet-stream", "bad".getBytes());

        assertThatThrownBy(() -> documentService.upload(1L, file, "alice"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("仅支持");
    }

    @Test
    void uploadSavesDocumentAndTriggersParse() throws Exception {
        KnowledgeBase kb = knowledgeBase(1L, 1L);
        User alice = user(1L, "alice", "ROLE_USER");
        when(knowledgeBaseRepository.findById(1L)).thenReturn(Optional.of(kb));
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(alice));
        when(documentRepository.existsByKnowledgeBaseIdAndFileHash(eq(1L), any())).thenReturn(false);
        when(documentRepository.save(any(Document.class))).thenAnswer(invocation -> {
            Document document = invocation.getArgument(0);
            document.setId(10L);
            return document;
        });

        MockMultipartFile file = new MockMultipartFile(
                "file", "notes.md", "text/markdown", "# 标题\n内容".getBytes());

        TransactionSynchronizationManager.initSynchronization();
        try {
            DocumentResponse response = documentService.upload(1L, file, "alice");

            assertThat(response.id()).isEqualTo(10L);
            assertThat(response.status()).isEqualTo("UPLOADED");
            verify(documentParser, never()).parseAsync(anyLong());

            // 模拟事务提交后再触发异步解析
            TransactionSynchronizationManager.getSynchronizations()
                    .forEach(TransactionSynchronization::afterCommit);
            verify(documentParser).parseAsync(10L);
            verify(fileStorage).save(eq(10L), eq("notes.md"), eq(file));
        } finally {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    @Test
    void uploadRejectsDuplicateDocument() throws Exception {
        KnowledgeBase kb = knowledgeBase(1L, 1L);
        User alice = user(1L, "alice", "ROLE_USER");
        when(knowledgeBaseRepository.findById(1L)).thenReturn(Optional.of(kb));
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(alice));
        when(documentRepository.existsByKnowledgeBaseIdAndFileHash(eq(1L), any())).thenReturn(true);

        MockMultipartFile file = new MockMultipartFile(
                "file", "notes.md", "text/markdown", "same content".getBytes());

        assertThatThrownBy(() -> documentService.upload(1L, file, "alice"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("已存在");
    }

    @Test
    void nonOwnerCannotUpload() {
        KnowledgeBase kb = knowledgeBase(1L, 1L);
        User bob = user(2L, "bob", "ROLE_USER");
        when(knowledgeBaseRepository.findById(1L)).thenReturn(Optional.of(kb));
        when(userRepository.findByUsername("bob")).thenReturn(Optional.of(bob));

        MockMultipartFile file = new MockMultipartFile(
                "file", "notes.md", "text/markdown", "content".getBytes());

        assertThatThrownBy(() -> documentService.upload(1L, file, "bob"))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void listReturnsDocuments() {
        Document document = new Document();
        document.setId(1L);
        document.setKnowledgeBaseId(1L);
        document.setFilename("a.md");
        document.setStatus("READY");
        when(documentRepository.findByKnowledgeBaseIdOrderByCreatedAtDesc(1L))
                .thenReturn(List.of(document));

        List<DocumentResponse> responses = documentService.list(1L);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).filename()).isEqualTo("a.md");
    }

    @Test
    void ownerCanDeleteDocument() throws Exception {
        Document document = new Document();
        document.setId(5L);
        document.setKnowledgeBaseId(1L);
        document.setFilename("a.md");

        KnowledgeBase kb = knowledgeBase(1L, 1L);
        User alice = user(1L, "alice", "ROLE_USER");
        when(documentRepository.findById(5L)).thenReturn(Optional.of(document));
        when(knowledgeBaseRepository.findById(1L)).thenReturn(Optional.of(kb));
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(alice));

        documentService.delete(5L, "alice");

        verify(documentRepository).delete(document);
        verify(fileStorage).delete(5L, "a.md");
    }

    private KnowledgeBase knowledgeBase(Long id, Long ownerId) {
        KnowledgeBase kb = new KnowledgeBase();
        kb.setId(id);
        kb.setOwnerId(ownerId);
        kb.setStatus("ACTIVE");
        return kb;
    }

    private User user(Long id, String username, String role) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setRole(role);
        return user;
    }
}
