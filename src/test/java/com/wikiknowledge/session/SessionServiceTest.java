package com.wikiknowledge.session;

import com.wikiknowledge.domain.KnowledgeBase;
import com.wikiknowledge.domain.Session;
import com.wikiknowledge.domain.User;
import com.wikiknowledge.exception.BusinessException;
import com.wikiknowledge.repository.KnowledgeBaseRepository;
import com.wikiknowledge.repository.MessageRepository;
import com.wikiknowledge.repository.SessionRepository;
import com.wikiknowledge.repository.UserRepository;
import com.wikiknowledge.session.dto.CreateSessionRequest;
import com.wikiknowledge.session.dto.SessionResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SessionServiceTest {

    @Mock
    private SessionRepository sessionRepository;

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private KnowledgeBaseRepository knowledgeBaseRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private SessionService sessionService;

    @Test
    void createSessionSetsUserAndKnowledgeBase() {
        KnowledgeBase kb = knowledgeBase(1L);
        User alice = user(1L, "alice");
        when(knowledgeBaseRepository.findById(1L)).thenReturn(Optional.of(kb));
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(alice));
        when(sessionRepository.save(any(Session.class))).thenAnswer(invocation -> {
            Session session = invocation.getArgument(0);
            session.setId(10L);
            return session;
        });

        Session session = sessionService.createSession(
                new CreateSessionRequest(1L, "Java 问答"),
                "alice"
        );

        assertThat(session.getId()).isEqualTo(10L);
        assertThat(session.getUserId()).isEqualTo(1L);
        assertThat(session.getKnowledgeBaseId()).isEqualTo(1L);
    }

    @Test
    void listReturnsOwnedSessions() {
        User alice = user(1L, "alice");
        Session session = new Session();
        session.setId(1L);
        session.setTitle("会话一");
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(alice));
        when(sessionRepository.findByUserIdOrderByUpdatedAtDesc(1L)).thenReturn(List.of(session));

        List<SessionResponse> responses = sessionService.list("alice");

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).title()).isEqualTo("会话一");
    }

    @Test
    void deleteOwnedSession() {
        User alice = user(1L, "alice");
        Session session = new Session();
        session.setId(1L);
        session.setUserId(1L);
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(alice));
        when(sessionRepository.findById(1L)).thenReturn(Optional.of(session));

        sessionService.delete(1L, "alice");

        verify(sessionRepository).delete(session);
    }

    @Test
    void cannotAccessOtherUsersSession() {
        User bob = user(2L, "bob");
        Session session = new Session();
        session.setId(1L);
        session.setUserId(1L);
        when(userRepository.findByUsername("bob")).thenReturn(Optional.of(bob));
        when(sessionRepository.findById(1L)).thenReturn(Optional.of(session));

        assertThatThrownBy(() -> sessionService.getOwnedSession(1L, "bob"))
                .isInstanceOf(BusinessException.class)
                .hasMessage("会话不存在");
    }

    private KnowledgeBase knowledgeBase(Long id) {
        KnowledgeBase kb = new KnowledgeBase();
        kb.setId(id);
        kb.setStatus("ACTIVE");
        return kb;
    }

    private User user(Long id, String username) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        return user;
    }
}
