package com.miaohome.service.file;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件存储服务接口
 * 抽象底层存储（本地磁盘 / 阿里云对象存储），上层业务无需关心具体实现。
 */
public interface FileStorageService {

    /** 公共读对象目录前缀，前缀内对象由 Bucket Policy 授匿名只读 */
    String PUBLIC_PREFIX = "public/";

    /**
     * 存储上传文件
     * @param file 上传的 MultipartFile
     * @return 文件存储路径 / key（统一为 public/yyyyMMdd/uuid.ext，可用于后续加载和删除）
     */
    String storeFile(MultipartFile file);

    /**
     * 根据路径 / key 加载文件为 Resource
     * @param relativePath 文件路径 / key
     * @return 可读的文件资源
     */
    Resource loadFile(String relativePath);

    /**
     * 根据路径 / key 删除文件
     * @param relativePath 文件路径 / key
     */
    void deleteFile(String relativePath);

    /**
     * 将存储的路径 / key 解析为可直接访问的 URL
     * <ul>
     *   <li>null / 空字符串 → 返回 null</li>
     *   <li>已是完整 http(s) URL（如微信头像）→ 原样返回</li>
     *   <li>public/ 前缀 → 返回公共读固定 URL</li>
     *   <li>其余（私有对象）→ 返回带签名临时 URL（本地存储返回相对路径）</li>
     * </ul>
     * @param key 文件路径 / key，或完整 URL
     * @return 可直接访问的 URL
     */
    String generateAccessUrl(String key);
}
