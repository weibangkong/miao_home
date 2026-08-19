package com.miaohome.service;

import com.miaohome.entity.CatMedia;
import com.miaohome.exception.BusinessException;
import com.miaohome.exception.ErrorCode;
import com.miaohome.repository.CatMediaRepository;
import com.miaohome.repository.CatRepository;
import com.miaohome.service.file.FileStorageService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 猫咪媒体业务（照片 / 视频）
 * <p>管理猫咪照片和视频的上传、删除、头像设置。</p>
 */
@Service
@Transactional
public class CatMediaService {

    private final CatMediaRepository catMediaRepository;
    private final CatRepository catRepository;
    private final FileStorageService fileStorageService;

    public CatMediaService(CatMediaRepository catMediaRepository,
                           CatRepository catRepository,
                           FileStorageService fileStorageService) {
        this.catMediaRepository = catMediaRepository;
        this.catRepository = catRepository;
        this.fileStorageService = fileStorageService;
    }

    /** 为猫咪添加一条媒体记录，若 isAvatar 为 true 则同步更新猫咪头像 */
    public CatMedia addMedia(Long catId, String mediaUrl, CatMedia.MediaType mediaType,
                             String ageStage, String fileName, Long fileSize, Boolean isAvatar) {
        // 获取猫咪的租户 ID
        Long tenantId = catRepository.findById(catId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CAT_NOT_FOUND, "猫咪不存在"))
                .getTenantId();

        CatMedia media = new CatMedia();
        media.setCatId(catId);
        media.setTenantId(tenantId);
        media.setMediaType(mediaType);
        media.setUrl(mediaUrl);
        media.setAgeStage(ageStage);
        media.setFileName(fileName);
        media.setFileSize(fileSize);
        media.setIsAvatar(isAvatar);
        media.setCreatedAt(LocalDateTime.now());
        media = catMediaRepository.save(media);

        // 如果标记为头像，同步更新猫咪的 avatarUrl
        if (Boolean.TRUE.equals(isAvatar)) {
            catRepository.findById(catId).ifPresent(cat -> {
                cat.setAvatarUrl(mediaUrl);
                cat.setUpdatedAt(LocalDateTime.now());
                catRepository.save(cat);
            });
        }
        return media;
    }

    /** 获取猫咪的所有媒体记录，按创建时间倒序 */
    public List<CatMedia> getCatMedia(Long catId) {
        return catMediaRepository.findByCatIdOrderByCreatedAtDesc(catId);
    }

    /** 删除指定媒体记录，同时清理磁盘文件 */
    public void deleteMedia(Long mediaId) {
        CatMedia media = catMediaRepository.findById(mediaId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEDIA_NOT_FOUND, "媒体文件不存在"));
        fileStorageService.deleteFile(media.getUrl());
        catMediaRepository.delete(media);
    }

    /** 删除某只猫咪的全部媒体（清理磁盘文件 + 数据库记录） */
    public void deleteAllByCatId(Long catId) {
        List<CatMedia> mediaList = catMediaRepository.findByCatIdOrderByCreatedAtDesc(catId);
        for (CatMedia media : mediaList) {
            fileStorageService.deleteFile(media.getUrl());
        }
        catMediaRepository.deleteByCatId(catId);
    }

    /** 将指定的媒体设为猫咪头像，其他媒体取消头像标记 */
    public void setAvatar(Long catId, Long mediaId) {
        // 将该猫咪的所有媒体标记为"非头像"
        List<CatMedia> mediaList = catMediaRepository.findByCatIdOrderByCreatedAtDesc(catId);
        for (CatMedia m : mediaList) {
            m.setIsAvatar(m.getId().equals(mediaId));
            catMediaRepository.save(m);
        }
        // 更新猫咪表的头像 URL
        CatMedia avatar = catMediaRepository.findById(mediaId).orElse(null);
        if (avatar != null) {
            catRepository.findById(catId).ifPresent(cat -> {
                cat.setAvatarUrl(avatar.getUrl());
                cat.setUpdatedAt(LocalDateTime.now());
                catRepository.save(cat);
            });
        }
    }
}
