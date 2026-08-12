package com.wikiknowledge.knowledge;

import com.wikiknowledge.domain.KnowledgeBase;
import com.wikiknowledge.domain.User;
import com.wikiknowledge.exception.BusinessException;
import com.wikiknowledge.knowledge.dto.KnowledgeBaseCreateRequest;
import com.wikiknowledge.knowledge.dto.KnowledgeBaseResponse;
import com.wikiknowledge.knowledge.dto.KnowledgeBaseUpdateRequest;
import com.wikiknowledge.repository.KnowledgeBaseRepository;
import com.wikiknowledge.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;/** 知识库业务逻辑与权限控制 */


@Service
public class KnowledgeBaseService {

    private final KnowledgeBaseRepository knowledgeBaseRepository;
    private final UserRepository userRepository;

    public KnowledgeBaseService(KnowledgeBaseRepository knowledgeBaseRepository,
                                UserRepository userRepository) {
        this.knowledgeBaseRepository = knowledgeBaseRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<KnowledgeBaseResponse> list() {
        return knowledgeBaseRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(KnowledgeBaseResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public KnowledgeBaseResponse get(Long id) {
        return KnowledgeBaseResponse.from(findKnowledgeBase(id));
    }

    @Transactional
    public KnowledgeBaseResponse create(KnowledgeBaseCreateRequest request, String username) {
        User owner = findUser(username);
        KnowledgeBase knowledgeBase = new KnowledgeBase();
        knowledgeBase.setName(request.name());
        knowledgeBase.setDescription(request.description());
        knowledgeBase.setOwnerId(owner.getId());
        return KnowledgeBaseResponse.from(knowledgeBaseRepository.save(knowledgeBase));
    }

    @Transactional
    public KnowledgeBaseResponse update(Long id, KnowledgeBaseUpdateRequest request, String username) {
        KnowledgeBase knowledgeBase = findKnowledgeBase(id);
        requireOwnerOrAdmin(knowledgeBase, username);
        knowledgeBase.setName(request.name());
        knowledgeBase.setDescription(request.description());
        return KnowledgeBaseResponse.from(knowledgeBaseRepository.save(knowledgeBase));
    }

    @Transactional
    public void delete(Long id, String username) {
        KnowledgeBase knowledgeBase = findKnowledgeBase(id);
        requireOwnerOrAdmin(knowledgeBase, username);
        knowledgeBaseRepository.delete(knowledgeBase);
    }

    private KnowledgeBase findKnowledgeBase(Long id) {
        return knowledgeBaseRepository.findById(id)
                .orElseThrow(() -> new BusinessException("KNOWLEDGE_BASE_NOT_FOUND", "知识库不存在"));
    }

    /**
     * 权限校验：只有知识库创建者或管理员可以操作。
     */
    private void requireOwnerOrAdmin(KnowledgeBase knowledgeBase, String username) {
        User user = findUser(username);
        boolean isAdmin = "ROLE_ADMIN".equals(user.getRole());
        if (isAdmin || user.getId().equals(knowledgeBase.getOwnerId())) {
            return;
        }
        throw new AccessDeniedException("没有权限操作该知识库");
    }

    private User findUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "用户不存在"));
    }
}
