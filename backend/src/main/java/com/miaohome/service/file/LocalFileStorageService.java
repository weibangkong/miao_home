package com.miaohome.service.file;

import com.miaohome.exception.BusinessException;
import com.miaohome.exception.ErrorCode;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * 本地磁盘文件存储
 * 按日期分目录存储到本地磁盘，开发环境默认使用。
 */
@Service
@ConditionalOnProperty(name = "storage.type", havingValue = "local", matchIfMissing = true)
public class LocalFileStorageService implements FileStorageService {

    @Value("${app.upload-dir:${user.dir}/uploads}")
    private String uploadDir;

    private Path uploadPath;

    /** 初始化上传目录，确保目录存在 */
    @PostConstruct
    public void init() {
        uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(uploadPath);
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.UPLOAD_DIR_INIT_ERROR, "无法创建上传目录: " + uploadPath, e);
        }
    }

    @Override
    public String storeFile(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String fileName = UUID.randomUUID().toString() + extension;
        String relativePath = PUBLIC_PREFIX + dateStr + "/" + fileName;

        try {
            Path targetDir = uploadPath.resolve(dateStr);
            Files.createDirectories(targetDir);

            Path targetPath = targetDir.resolve(fileName);
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            return relativePath;
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.FILE_UPLOAD_ERROR, "文件上传失败: " + originalFilename, e);
        }
    }

    @Override
    public Resource loadFile(String relativePath) {
        try {
            Path file = uploadPath.resolve(relativePath).normalize();
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new BusinessException(ErrorCode.FILE_NOT_FOUND, "文件不存在: " + relativePath);
            }
        } catch (MalformedURLException e) {
            throw new BusinessException(ErrorCode.FILE_NOT_FOUND, "文件路径错误: " + relativePath, e);
        }
    }

    @Override
    public void deleteFile(String relativePath) {
        try {
            Path file = uploadPath.resolve(relativePath).normalize();
            Files.deleteIfExists(file);
        } catch (IOException e) {
            // 删除失败不做抛出处理，避免阻塞业务
        }
    }

    @Override
    public String generateAccessUrl(String relativePath) {
        if (relativePath == null || relativePath.isEmpty()) {
            return null;
        }
        // 已是完整 URL（如微信头像），原样返回
        if (relativePath.startsWith("http://") || relativePath.startsWith("https://")) {
            return relativePath;
        }
        // 本地存储无需签名，直接返回相对路径，由前端拼后端 /files 代理访问
        return relativePath;
    }
}
