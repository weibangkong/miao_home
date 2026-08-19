package com.miaohome.controller;

import com.miaohome.dto.*;
import com.miaohome.exception.BusinessException;
import com.miaohome.exception.ErrorCode;
import com.miaohome.entity.Cat;
import com.miaohome.entity.CatHealthRecord;
import com.miaohome.entity.CatMedia;
import com.miaohome.service.CatHealthRecordService;
import com.miaohome.service.CatLocationService;
import com.miaohome.service.CatMediaService;
import com.miaohome.service.CatService;
import com.miaohome.service.file.FileStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 猫咪相关接口
 */
@Tag(name = "猫咪管理", description = "猫咪基础信息、媒体、健康记录与出没地点")
@RestController
@RequestMapping("/cats")
public class CatController {

    private final CatService catService;
    private final CatMediaService catMediaService;
    private final CatHealthRecordService catHealthRecordService;
    private final CatLocationService catLocationService;
    private final FileStorageService fileStorageService;

    public CatController(CatService catService,
                         CatMediaService catMediaService,
                         CatHealthRecordService catHealthRecordService,
                         CatLocationService catLocationService,
                         FileStorageService fileStorageService) {
        this.catService = catService;
        this.catMediaService = catMediaService;
        this.catHealthRecordService = catHealthRecordService;
        this.catLocationService = catLocationService;
        this.fileStorageService = fileStorageService;
    }

    /** 允许的媒体文件扩展名（图片 + 视频） */
    private static final Set<String> ALLOWED_MEDIA_EXTENSIONS = Set.of(
            "jpg", "jpeg", "png", "gif", "webp", "mp4", "mov", "m4v", "webm");

    /** 媒体文件大小上限（字节），50MB */
    private static final long MAX_MEDIA_BYTES = 50L * 1024 * 1024;

    // ==================== 猫咪基础接口 ====================

    /**
     * 获取猫咪列表
     */
    @Operation(summary = "获取猫咪列表")
    @GetMapping("/list")
    public ApiResult<List<CatResponse>> list() {
        List<Cat> cats = catService.getAllCats();
        List<CatResponse> result = cats.stream().map(cat -> {
            CatResponse resp = toCatResponse(cat);
            resp.setLocationList(catLocationService.getByCatIdAndTenantId(cat.getId(), cat.getTenantId()));
            return resp;
        }).collect(Collectors.toList());
        return ApiResult.success(result);
    }

    /**
     * 获取猫咪详情（含媒体列表、健康记录、出没地点）
     */
    @Operation(summary = "获取猫咪详情")
    @GetMapping("/{id}")
    public ApiResult<CatResponse> detail(@Parameter(description = "猫咪 ID") @PathVariable Long id) {
        Cat cat = catService.getCatById(id);
        CatResponse resp = toCatResponse(cat);
        resp.setMediaList(catMediaService.getCatMedia(id).stream()
                .map(this::toCatMediaResponse).collect(Collectors.toList()));
        resp.setHealthRecordList(catHealthRecordService.getHealthRecords(id).stream()
                .map(this::toHealthRecordResponse).collect(Collectors.toList()));
        resp.setLocationList(catLocationService.getByCatIdAndTenantId(cat.getId(), cat.getTenantId()));
        return ApiResult.success(resp);
    }

    /** 创建猫咪 */
    @Operation(summary = "创建猫咪")
    @PostMapping(consumes = "application/json")
    public ApiResult<CatResponse> create(@Parameter(description = "猫咪信息") @RequestBody CatCreateRequest request) {
        Cat cat = new Cat();
        cat.setName(request.getName());
        cat.setColor(request.getColor());
        cat.setGender(request.getGender());
        cat.setBirthYear(request.getBirthYear());
        cat.setDescription(request.getDescription());
        cat.setIsNeutered(request.getIsNeutered());
        cat = catService.createCat(cat);

        // 保存出没地点
        if (request.getFrequentCommunities() != null) {
            catLocationService.saveOrUpdate(
                    cat.getId(), cat.getTenantId(), request.getFrequentCommunities());
        }

        return ApiResult.success(toCatResponse(cat));
    }

    /** 更新猫咪信息 */
    @Operation(summary = "更新猫咪信息")
    @PutMapping(value = "/{id}", consumes = "application/json")
    public ApiResult<CatResponse> update(
            @Parameter(description = "猫咪 ID") @PathVariable Long id,
            @Parameter(description = "猫咪信息") @RequestBody CatCreateRequest request) {
        Cat cat = new Cat();
        cat.setName(request.getName());
        cat.setColor(request.getColor());
        cat.setGender(request.getGender());
        cat.setBirthYear(request.getBirthYear());
        cat.setDescription(request.getDescription());
        cat.setIsNeutered(request.getIsNeutered());
        cat = catService.updateCat(id, cat);

        // 同步出没地点
        if (request.getFrequentCommunities() != null) {
            catLocationService.saveOrUpdate(
                    cat.getId(), cat.getTenantId(), request.getFrequentCommunities());
        }

        return ApiResult.success(toCatResponse(cat));
    }

    /** 删除猫咪（级联删除媒体文件、媒体记录、健康记录、出没地点） */
    @Operation(summary = "删除猫咪")
    @DeleteMapping("/{id}")
    public ApiResult<Void> delete(@Parameter(description = "猫咪 ID") @PathVariable Long id) {
        Cat cat = catService.getCatById(id);
        catMediaService.deleteAllByCatId(id);
        catHealthRecordService.deleteAllByCatId(id);
        catLocationService.deleteByCatIdAndTenantId(id, cat.getTenantId());
        catService.deleteCat(id);
        return ApiResult.success(null);
    }

    // ==================== 媒体接口 ====================

    /**
     * 上传猫咪照片或视频
     * @param id 猫咪 ID
     * @param file 媒体文件
     * @param ageStage 年龄阶段
     * @param isAvatar 是否同时设为头像
     */
    @Operation(summary = "上传猫咪照片或视频（后端代理直传）")
    @PostMapping(value = "/{id}/media", consumes = "multipart/form-data")
    public ApiResult<CatMediaResponse> uploadMedia(
            @Parameter(description = "猫咪 ID") @PathVariable Long id,
            @Parameter(description = "媒体文件") @RequestParam("file") MultipartFile file,
            @Parameter(description = "年龄阶段") @RequestParam(required = false) String ageStage,
            @Parameter(description = "是否设为头像") @RequestParam(defaultValue = "false") boolean isAvatar) {

        String url = fileStorageService.storeFile(file);
        String contentType = file.getContentType();
        CatMedia.MediaType mediaType = (contentType != null && contentType.startsWith("video"))
                ? CatMedia.MediaType.VIDEO : CatMedia.MediaType.PHOTO;

        CatMedia media = catMediaService.addMedia(id, url, mediaType, ageStage,
                file.getOriginalFilename(), file.getSize(), isAvatar);
        return ApiResult.success(toCatMediaResponse(media));
    }

    /**
     * 确认前端直传结果（回传 OSS 对象键，仅落库元数据）
     * @param id 猫咪 ID
     * @param request 对象键及媒体元数据
     */
    @Operation(summary = "确认前端直传结果（回传 OSS 对象键，仅落库元数据）")
    @PostMapping(value = "/{id}/media/confirm", consumes = "application/json")
    public ApiResult<CatMediaResponse> confirmMediaObject(
            @Parameter(description = "猫咪 ID") @PathVariable Long id,
            @Parameter(description = "对象键及媒体元数据") @RequestBody CatMediaObjectRequest request) {

        String objectKey = request.getObjectKey();
        if (objectKey == null || !objectKey.matches("^public/\\d{8}/[\\w.\\-]+$")) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "非法的对象键: " + objectKey);
        }
        validateMediaMeta(request.getFileSize(), objectKey);

        CatMedia.MediaType mediaType = "VIDEO".equalsIgnoreCase(request.getMediaType())
                ? CatMedia.MediaType.VIDEO : CatMedia.MediaType.PHOTO;

        CatMedia media = catMediaService.addMedia(id, objectKey, mediaType, request.getAgeStage(),
                request.getFileName(), request.getFileSize(), request.getIsAvatar());
        return ApiResult.success(toCatMediaResponse(media));
    }

    /**
     * 校验媒体元数据：文件大小与扩展名白名单
     */
    private void validateMediaMeta(Long fileSize, String objectKey) {
        if (fileSize == null || fileSize <= 0 || fileSize > MAX_MEDIA_BYTES) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "非法的文件大小: " + fileSize);
        }
        int dot = objectKey.lastIndexOf('.');
        String ext = dot >= 0 ? objectKey.substring(dot + 1).toLowerCase() : "";
        if (!ALLOWED_MEDIA_EXTENSIONS.contains(ext)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "不支持的文件类型: " + ext);
        }
    }

    /** 删除指定媒体文件 */
    @Operation(summary = "删除指定媒体文件")
    @DeleteMapping("/media/{mediaId}")
    public ApiResult<Void> deleteMedia(@Parameter(description = "媒体 ID") @PathVariable Long mediaId) {
        catMediaService.deleteMedia(mediaId);
        return ApiResult.success(null);
    }

    /** 将指定媒体设为猫咪头像 */
    @Operation(summary = "将指定媒体设为猫咪头像")
    @PostMapping(value = "/{catId}/avatar/{mediaId}", consumes = "application/json")
    public ApiResult<CatResponse> setAvatar(
            @Parameter(description = "猫咪 ID") @PathVariable Long catId,
            @Parameter(description = "媒体 ID") @PathVariable Long mediaId) {
        catMediaService.setAvatar(catId, mediaId);
        Cat cat = catService.getCatById(catId);
        return ApiResult.success(toCatResponse(cat));
    }

    // ==================== 健康记录接口 ====================

    /** 获取猫咪的健康记录列表 */
    @Operation(summary = "获取猫咪的健康记录列表")
    @GetMapping("/{catId}/health/records/list")
    public ApiResult<List<CatHealthRecordResponse>> listHealthRecords(
            @Parameter(description = "猫咪 ID") @PathVariable Long catId) {
        List<CatHealthRecord> records = catHealthRecordService.getHealthRecords(catId);
        List<CatHealthRecordResponse> result = records.stream()
                .map(this::toHealthRecordResponse).collect(Collectors.toList());
        return ApiResult.success(result);
    }

    /** 添加健康记录 */
    @Operation(summary = "添加健康记录")
    @PostMapping(value = "/{catId}/health/records", consumes = "application/json")
    public ApiResult<CatHealthRecordResponse> addHealthRecord(
            @Parameter(description = "猫咪 ID") @PathVariable Long catId,
            @Parameter(description = "健康记录信息") @RequestBody CatHealthRecordRequest request) {
        CatHealthRecord record = new CatHealthRecord();
        record.setIsSick(request.getIsSick());
        record.setDiseaseName(request.getDiseaseName());
        record.setDescription(request.getDescription());
        record.setTreatment(request.getTreatment());
        if (request.getRecordDate() != null) record.setRecordDate(LocalDate.parse(request.getRecordDate()));
        record = catHealthRecordService.addHealthRecord(catId, record);
        return ApiResult.success(toHealthRecordResponse(record));
    }

    /** 更新健康记录 */
    @Operation(summary = "更新健康记录")
    @PutMapping(value = "/{catId}/health/records/{recordId}", consumes = "application/json")
    public ApiResult<CatHealthRecordResponse> updateHealthRecord(
            @Parameter(description = "猫咪 ID") @PathVariable Long catId,
            @Parameter(description = "健康记录 ID") @PathVariable Long recordId,
            @Parameter(description = "健康记录信息") @RequestBody CatHealthRecordRequest request) {
        CatHealthRecord record = new CatHealthRecord();
        record.setIsSick(request.getIsSick());
        record.setDiseaseName(request.getDiseaseName());
        record.setDescription(request.getDescription());
        record.setTreatment(request.getTreatment());
        if (request.getRecordDate() != null) record.setRecordDate(LocalDate.parse(request.getRecordDate()));
        record = catHealthRecordService.updateHealthRecord(recordId, record);
        return ApiResult.success(toHealthRecordResponse(record));
    }

    /** 删除健康记录 */
    @Operation(summary = "删除健康记录")
    @DeleteMapping("/{catId}/health/records/{recordId}")
    public ApiResult<Void> deleteHealthRecord(
            @Parameter(description = "猫咪 ID") @PathVariable Long catId,
            @Parameter(description = "健康记录 ID") @PathVariable Long recordId) {
        catHealthRecordService.deleteHealthRecord(recordId);
        return ApiResult.success(null);
    }

    // ==================== 内部转换方法 ====================

    /** Cat 实体 → CatResponse */
    private CatResponse toCatResponse(Cat cat) {
        CatResponse resp = new CatResponse();
        resp.setId(cat.getId());
        resp.setTenantId(cat.getTenantId());
        resp.setName(cat.getName());
        resp.setColor(cat.getColor());
        resp.setGender(cat.getGender());
        resp.setBirthYear(cat.getBirthYear());
        resp.setDescription(cat.getDescription());
        resp.setAvatarUrl(cat.getAvatarUrl() != null
                ? fileStorageService.generateAccessUrl(cat.getAvatarUrl()) : null);
        resp.setIsAdopted(cat.getIsAdopted());
        resp.setIsNeutered(cat.getIsNeutered());
        resp.setLikeCount(cat.getLikeCount());
        resp.setCreatedAt(cat.getCreatedAt() != null ? cat.getCreatedAt().toString() : null);
        resp.setUpdatedAt(cat.getUpdatedAt() != null ? cat.getUpdatedAt().toString() : null);
        return resp;
    }

    /** CatMedia 实体 → CatMediaResponse */
    private CatMediaResponse toCatMediaResponse(CatMedia media) {
        CatMediaResponse resp = new CatMediaResponse();
        resp.setId(media.getId());
        resp.setCatId(media.getCatId());
        resp.setMediaType(media.getMediaType().name());
        resp.setUrl(fileStorageService.generateAccessUrl(media.getUrl()));
        resp.setAgeStage(media.getAgeStage());
        resp.setIsAvatar(media.getIsAvatar());
        resp.setFileName(media.getFileName());
        resp.setFileSize(media.getFileSize());
        resp.setCreatedAt(media.getCreatedAt() != null ? media.getCreatedAt().toString() : null);
        return resp;
    }

    /** CatHealthRecord 实体 → CatHealthRecordResponse */
    private CatHealthRecordResponse toHealthRecordResponse(CatHealthRecord record) {
        CatHealthRecordResponse resp = new CatHealthRecordResponse();
        resp.setId(record.getId());
        resp.setCatId(record.getCatId());
        resp.setIsSick(record.getIsSick());
        resp.setDiseaseName(record.getDiseaseName());
        resp.setDescription(record.getDescription());
        resp.setTreatment(record.getTreatment());
        resp.setRecordDate(record.getRecordDate() != null ? record.getRecordDate().toString() : null);
        resp.setCreatedAt(record.getCreatedAt() != null ? record.getCreatedAt().toString() : null);
        return resp;
    }
}
