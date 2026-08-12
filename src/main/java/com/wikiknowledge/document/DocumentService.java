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
import java.util.Set;/** 文档业务逻辑：上传、列表、删除、权限校验 */


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

    /**
     * 上传文档：校验格式与大小 -> 去重 -> 落库 -> 保存文件 -> 触发异步解析。
     */
    @Transactional
    public DocumentResponse upload(Long knowledgeBaseId, MultipartFile file, String username)
            throws IOException, NoSuchAlgorithmException {
        // 1. 文件基础校验
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

        // 2. 校验知识库权限
        KnowledgeBase knowledgeBase = knowledgeBaseRepository.findById(knowledgeBaseId)
                .orElseThrow(() -> new BusinessException("KNOWLEDGE_BASE_NOT_FOUND", "知识库不存在"));
        requireOwnerOrAdmin(knowledgeBase, username);

        // 3. 使用 SHA-256 去重
        String fileHash = sha256(file.getBytes());
        if (documentRepository.existsByKnowledgeBaseIdAndFileHash(knowledgeBase.getId(), fileHash)) {
            throw new BusinessException("DUPLICATE_DOCUMENT", "同一知识库中已存在相同文件");
        }

        // 4. 保存文档记录与文件，再触发异步解析
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

    /**
     * 删除文档：先校验权限，再删除数据库记录和本地文件。
     */
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

    /**
     * 权限校验：只有知识库创建者或管理员可以操作。
     */
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

    /**
     * 计算文件 SHA-256，作为内容指纹。
     */
    private String sha256(byte[] bytes) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        return HexFormat.of().formatHex(digest.digest(bytes));
    }
}
