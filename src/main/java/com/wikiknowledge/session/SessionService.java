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
import com.wikiknowledge.session.dto.MessageResponse;
import com.wikiknowledge.session.dto.SessionDetailResponse;
import com.wikiknowledge.session.dto.SessionResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;/** 会话业务逻辑 */


@Service
public class SessionService {

    private final SessionRepository sessionRepository;
    private final MessageRepository messageRepository;
    private final KnowledgeBaseRepository knowledgeBaseRepository;
    private final UserRepository userRepository;

    public SessionService(SessionRepository sessionRepository,
                          MessageRepository messageRepository,
                          KnowledgeBaseRepository knowledgeBaseRepository,
                          UserRepository userRepository) {
        this.sessionRepository = sessionRepository;
        this.messageRepository = messageRepository;
        this.knowledgeBaseRepository = knowledgeBaseRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Session createSession(CreateSessionRequest request, String username) {
        KnowledgeBase knowledgeBase = knowledgeBaseRepository.findById(request.knowledgeBaseId())
                .orElseThrow(() -> new BusinessException("KNOWLEDGE_BASE_NOT_FOUND", "知识库不存在"));
        User user = findUser(username);
        Session session = new Session();
        session.setUserId(user.getId());
        session.setKnowledgeBaseId(knowledgeBase.getId());
        session.setTitle(request.title() == null || request.title().isBlank() ? "新会话" : request.title());
        return sessionRepository.save(session);
    }

    @Transactional(readOnly = true)
    public List<SessionResponse> list(String username) {
        User user = findUser(username);
        return sessionRepository.findByUserIdOrderByUpdatedAtDesc(user.getId()).stream()
                .map(SessionResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public SessionDetailResponse get(Long id, String username) {
        Session session = getOwnedSession(id, username);
        List<MessageResponse> messages = messageRepository.findBySessionIdOrderByIdAsc(id).stream()
                .map(MessageResponse::from)
                .toList();
        return new SessionDetailResponse(SessionResponse.from(session), messages);
    }

    @Transactional
    public void delete(Long id, String username) {
        sessionRepository.delete(getOwnedSession(id, username));
    }

    /**
     * 查找当前用户自己的会话；越权访问统一返回“会话不存在”。
     */
    @Transactional(readOnly = true)
    public Session getOwnedSession(Long id, String username) {
        User user = findUser(username);
        return sessionRepository.findById(id)
                .filter(session -> session.getUserId().equals(user.getId()))
                .orElseThrow(() -> new BusinessException("SESSION_NOT_FOUND", "会话不存在"));
    }

    private User findUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "用户不存在"));
    }
}
