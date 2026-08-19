package com.miaohome.service.file;

import com.aliyun.oss.OSS;
import com.aliyun.oss.model.OSSObject;
import com.aliyun.oss.model.ObjectMetadata;
import com.miaohome.config.StorageConfig;
import com.miaohome.exception.BusinessException;
import com.miaohome.exception.ErrorCode;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.UUID;

/**
 * 阿里云 OSS 对象存储
 * 当 storage.type=aliyun 时生效，负责服务端对象管理（上传 / 删除 / 读取）。
 */
@Service
@ConditionalOnProperty(name = "storage.type", havingValue = "aliyun")
public class AliyunOssStorageService implements FileStorageService {

    private final OSS ossClient;
    private final StorageConfig.AliyunOssProperties props;

    public AliyunOssStorageService(OSS ossClient, StorageConfig.AliyunOssProperties props) {
        this.ossClient = ossClient;
        this.props = props;
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
        String key = PUBLIC_PREFIX + dateStr + "/" + fileName;

        String contentType = file.getContentType();
        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        try {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(contentType);
            metadata.setContentLength(file.getSize());
            ossClient.putObject(props.getBucket(), key, file.getInputStream(), metadata);
            return key;
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.FILE_UPLOAD_ERROR,
                    "文件上传失败: " + originalFilename, e);
        }
    }

    @Override
    public Resource loadFile(String key) {
        try {
            OSSObject object = ossClient.getObject(props.getBucket(), key);
            return new InputStreamResource(object.getObjectContent());
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.FILE_NOT_FOUND,
                    "文件不存在: " + key, e);
        }
    }

    @Override
    public void deleteFile(String key) {
        try {
            ossClient.deleteObject(props.getBucket(), key);
        } catch (Exception e) {
            // 删除失败不做抛出处理，避免阻塞业务
        }
    }

    @Override
    public String generateAccessUrl(String key) {
        if (key == null || key.isEmpty()) {
            return null;
        }
        // 已是完整 URL（如微信头像），原样返回
        if (key.startsWith("http://") || key.startsWith("https://")) {
            return key;
        }
        // 公共读对象返回固定 URL，前端直连回显，无需签名
        if (key.startsWith(PUBLIC_PREFIX)) {
            return buildPublicBaseUrl() + "/" + key;
        }
        // 私有对象走临时签名 URL
        Date expiration = new Date(
                System.currentTimeMillis() + props.getUrlExpirationSeconds() * 1000);
        URL url = ossClient.generatePresignedUrl(props.getBucket(), key, expiration);
        return url.toString();
    }

    /**
     * 解析公共读对象访问前缀
     * 优先使用显式配置的 public-base-url（为将来切 CDN 域名预留），
     * 未配置时由 endpoint + bucket 推导出 https://bucket.<endpoint-host> 形式。
     */
    private String buildPublicBaseUrl() {
        String configured = props.getPublicBaseUrl();
        if (configured != null && !configured.isBlank()) {
            return configured.replaceAll("/+$", "");
        }
        String host = props.getEndpoint().replaceFirst("^https?://", "").replaceAll("/+$", "");
        return "https://" + props.getBucket() + "." + host;
    }
}
