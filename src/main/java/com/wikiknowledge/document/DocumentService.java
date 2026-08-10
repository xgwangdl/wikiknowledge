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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class DocumentService {

    private static final long MAX_FILE_SIZE = 20L * 1024 * 1024;
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("pdf", "docx", "md", "txt");

    private final DocumentRepository documentRepository;
    private final KnowledgeBaseRepository knowledgeBaseRepository;
    private final UserRepository userRepository;
    private final LocalFileStorage fileStorage;
    private final DocumentParser documentParser;

    public DocumentService(DocumentRepository documentRepository,
                           KnowledgeBaseRepository knowledgeBaseRepository,
                           UserRepository userRepository,
                           LocalFileStorage fileStorage,
                           DocumentParser documentParser) {
        this.documentRepository = documentRepository;
        this.knowledgeBaseRepository = knowledgeBaseRepository;
        this.userRepository = userRepository;
        this.fileStorage = fileStorage;
        this.documentParser = documentParser;
    }

    @Transactional
    public DocumentResponse upload(Long knowledgeBaseId, MultipartFile file, String username)
            throws IOException, NoSuchAlgorithmException {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("EMPTY_FILE", "文件不能为空");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException("FILE_TOO_LARGE", "文件大小不能超过 20MB");
        }
        String filename = file.getOriginalFilename();
        String extension = extension(filename);
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new BusinessException("UNSUPPORTED_FILE_TYPE", "仅支持 pdf/docx/md/txt 文件");
        }

        KnowledgeBase knowledgeBase = knowledgeBaseRepository.findById(knowledgeBaseId)
                .orElseThrow(() -> new BusinessException("KNOWLEDGE_BASE_NOT_FOUND", "知识库不存在"));
        requireOwnerOrAdmin(knowledgeBase, username);

        String fileHash = sha256(file.getBytes());
        if (documentRepository.existsByKnowledgeBaseIdAndFileHash(knowledgeBase.getId(), fileHash)) {
            throw new BusinessException("DUPLICATE_DOCUMENT", "同一知识库中已存在相同文件");
        }

        Document document = new Document();
        document.setKnowledgeBaseId(knowledgeBase.getId());
        document.setFilename(filename);
        document.setFileHash(fileHash);
        document.setContentType(file.getContentType());
        document.setFileSize(file.getSize());
        document.setStatus("UPLOADED");
        documentRepository.save(document);

        fileStorage.save(document.getId(), filename, file);
        documentParser.parseAsync(document.getId());
        return DocumentResponse.from(document);
    }

    @Transactional(readOnly = true)
    public List<DocumentResponse> list(Long knowledgeBaseId) {
        return documentRepository.findByKnowledgeBaseIdOrderByCreatedAtDesc(knowledgeBaseId).stream()
                .map(DocumentResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public DocumentResponse get(Long id) {
        return DocumentResponse.from(findDocument(id));
    }

    @Transactional
    public void delete(Long id, String username) throws IOException {
        Document document = findDocument(id);
        KnowledgeBase knowledgeBase = knowledgeBaseRepository.findById(document.getKnowledgeBaseId())
                .orElseThrow(() -> new BusinessException("KNOWLEDGE_BASE_NOT_FOUND", "知识库不存在"));
        requireOwnerOrAdmin(knowledgeBase, username);
        documentRepository.delete(document);
        fileStorage.delete(document.getId(), document.getFilename());
    }

    private Document findDocument(Long id) {
        return documentRepository.findById(id)
                .orElseThrow(() -> new BusinessException("DOCUMENT_NOT_FOUND", "文档不存在"));
    }

    private void requireOwnerOrAdmin(KnowledgeBase knowledgeBase, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "用户不存在"));
        boolean isAdmin = "ROLE_ADMIN".equals(user.getRole());
        if (isAdmin || user.getId().equals(knowledgeBase.getOwnerId())) {
            return;
        }
        throw new AccessDeniedException("没有权限操作该知识库");
    }

    private String extension(String filename) {
        if (filename == null) {
            return "";
        }
        int index = filename.lastIndexOf('.');
        return index < 0 ? "" : filename.substring(index + 1).toLowerCase(Locale.ROOT);
    }

    private String sha256(byte[] bytes) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        return HexFormat.of().formatHex(digest.digest(bytes));
    }
}
