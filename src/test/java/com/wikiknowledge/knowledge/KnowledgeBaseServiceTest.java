package com.wikiknowledge.knowledge;

import com.wikiknowledge.domain.KnowledgeBase;
import com.wikiknowledge.domain.User;
import com.wikiknowledge.exception.BusinessException;
import com.wikiknowledge.knowledge.dto.KnowledgeBaseCreateRequest;
import com.wikiknowledge.knowledge.dto.KnowledgeBaseResponse;
import com.wikiknowledge.knowledge.dto.KnowledgeBaseUpdateRequest;
import com.wikiknowledge.repository.KnowledgeBaseRepository;
import com.wikiknowledge.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KnowledgeBaseServiceTest {

    @Mock
    private KnowledgeBaseRepository knowledgeBaseRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private KnowledgeBaseService knowledgeBaseService;

    @Test
    void createSetsCurrentUserAsOwner() {
        User owner = user(1L, "alice", "ROLE_USER");
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(owner));
        when(knowledgeBaseRepository.save(any(KnowledgeBase.class))).thenAnswer(invocation -> {
            KnowledgeBase kb = invocation.getArgument(0);
            kb.setId(10L);
            return kb;
        });

        KnowledgeBaseResponse response = knowledgeBaseService.create(
                new KnowledgeBaseCreateRequest("Java 知识库", "Java 学习资料"),
                "alice"
        );

        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.ownerId()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("Java 知识库");
    }

    @Test
    void listReturnsAllKnowledgeBases() {
        KnowledgeBase first = knowledgeBase(1L, "A", null);
        KnowledgeBase second = knowledgeBase(2L, "B", null);
        when(knowledgeBaseRepository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of(first, second));

        List<KnowledgeBaseResponse> responses = knowledgeBaseService.list();

        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).name()).isEqualTo("A");
    }

    @Test
    void getThrowsWhenKnowledgeBaseMissing() {
        when(knowledgeBaseRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> knowledgeBaseService.get(99L))
                .isInstanceOf(BusinessException.class)
                .hasMessage("知识库不存在");
    }

    @Test
    void ownerCanUpdateKnowledgeBase() {
        KnowledgeBase kb = knowledgeBase(1L, "Old", 1L);
        when(knowledgeBaseRepository.findById(1L)).thenReturn(Optional.of(kb));
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user(1L, "alice", "ROLE_USER")));
        when(knowledgeBaseRepository.save(any(KnowledgeBase.class))).thenAnswer(invocation -> invocation.getArgument(0));

        KnowledgeBaseResponse response = knowledgeBaseService.update(
                1L,
                new KnowledgeBaseUpdateRequest("New", "New description"),
                "alice"
        );

        assertThat(response.name()).isEqualTo("New");
        assertThat(response.description()).isEqualTo("New description");
    }

    @Test
    void nonOwnerCannotUpdateKnowledgeBase() {
        KnowledgeBase kb = knowledgeBase(1L, "Old", 1L);
        when(knowledgeBaseRepository.findById(1L)).thenReturn(Optional.of(kb));
        when(userRepository.findByUsername("bob")).thenReturn(Optional.of(user(2L, "bob", "ROLE_USER")));

        assertThatThrownBy(() -> knowledgeBaseService.update(
                1L,
                new KnowledgeBaseUpdateRequest("New", null),
                "bob"
        )).isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void adminCanDeleteAnyKnowledgeBase() {
        KnowledgeBase kb = knowledgeBase(1L, "Old", 1L);
        when(knowledgeBaseRepository.findById(1L)).thenReturn(Optional.of(kb));
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user(99L, "admin", "ROLE_ADMIN")));

        knowledgeBaseService.delete(1L, "admin");

        verify(knowledgeBaseRepository).delete(kb);
    }

    @Test
    void nonOwnerCannotDeleteKnowledgeBase() {
        KnowledgeBase kb = knowledgeBase(1L, "Old", 1L);
        when(knowledgeBaseRepository.findById(1L)).thenReturn(Optional.of(kb));
        when(userRepository.findByUsername("bob")).thenReturn(Optional.of(user(2L, "bob", "ROLE_USER")));

        assertThatThrownBy(() -> knowledgeBaseService.delete(1L, "bob"))
                .isInstanceOf(AccessDeniedException.class);
        verify(knowledgeBaseRepository, never()).delete(any(KnowledgeBase.class));
    }

    private User user(Long id, String username, String role) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setRole(role);
        return user;
    }

    private KnowledgeBase knowledgeBase(Long id, String name, Long ownerId) {
        KnowledgeBase kb = new KnowledgeBase();
        kb.setId(id);
        kb.setName(name);
        kb.setOwnerId(ownerId);
        kb.setStatus("ACTIVE");
        return kb;
    }
}
