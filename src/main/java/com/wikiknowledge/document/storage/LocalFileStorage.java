package com.wikiknowledge.document.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;/** 本地文件存储 */


@Service
public class LocalFileStorage {

    private final Path root;

    public LocalFileStorage(@Value("${app.storage.root}") String root) throws IOException {
        this.root = Paths.get(root).toAbsolutePath().normalize();
        Files.createDirectories(this.root);
    }

    /**
     * 将上传文件保存到本地目录，路径按文档 ID 隔离。
     *
     * @param documentId 文档 ID
     * @param filename   原始文件名
     * @param file       上传文件
     * @return 保存后的文件路径
     * @throws IOException 文件写入失败
     */
    public Path save(Long documentId, String filename, MultipartFile file) throws IOException {
        Path directory = root.resolve(String.valueOf(documentId));
        Files.createDirectories(directory);
        Path target = directory.resolve(safeName(filename));
        file.transferTo(target);
        return target;
    }

    public InputStream read(Long documentId, String filename) throws IOException {
        return Files.newInputStream(resolve(documentId, filename));
    }

    public void delete(Long documentId, String filename) throws IOException {
        Files.deleteIfExists(resolve(documentId, filename));
    }

    private Path resolve(Long documentId, String filename) {
        return root.resolve(String.valueOf(documentId)).resolve(safeName(filename));
    }

    private String safeName(String filename) {
        return Paths.get(filename).getFileName().toString();
    }
}
