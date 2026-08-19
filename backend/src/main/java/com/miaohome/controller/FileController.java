package com.miaohome.controller;

import com.miaohome.dto.ApiResult;
import com.miaohome.dto.StsCredentialsResponse;
import com.miaohome.service.file.AliyunStsService;
import com.miaohome.service.file.FileStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 文件访问接口
 * <p>提供已上传文件的直接访问（图片预览、视频播放等）。</p>
 */
@Tag(name = "文件访问", description = "STS 直传凭证签发与文件访问")
@RestController
@RequestMapping("/files")
public class FileController {

    private final FileStorageService fileStorageService;
    private final AliyunStsService aliyunStsService;

    public FileController(FileStorageService fileStorageService, AliyunStsService aliyunStsService) {
        this.fileStorageService = fileStorageService;
        this.aliyunStsService = aliyunStsService;
    }

    /**
     * 获取前端直传 OSS 所需的 STS 临时凭证
     */
    @Operation(summary = "获取前端直传 OSS 所需的 STS 临时凭证")
    @PostMapping("/sts/credentials")
    public ApiResult<StsCredentialsResponse> stsCredentials() {
        return ApiResult.success(aliyunStsService.issueCredentials());
    }

    /**
     * 获取上传的文件
     * <p>URL 格式：/files/{relativePath}，与文件存储相对路径对应（如 public/yyyyMMdd/uuid.ext）。</p>
     * @param relativePath 文件相对路径（多段，含 public 前缀与日期目录）
     */
    @Operation(summary = "获取上传的文件")
    @GetMapping("/{*relativePath}")
    public ResponseEntity<Resource> getFile(
            @Parameter(description = "文件相对路径（如 public/yyyyMMdd/uuid.ext）") @PathVariable String relativePath) {
        Resource resource = fileStorageService.loadFile(relativePath);

        // 取最后一段作为文件名，按扩展名推断 Content-Type（本地存储与 OSS 统一生效）
        String fileName = relativePath.substring(relativePath.lastIndexOf('/') + 1);
        MediaType contentType = MediaTypeFactory.getMediaType(fileName)
                .orElse(MediaType.APPLICATION_OCTET_STREAM);

        return ResponseEntity.ok()
                .contentType(contentType)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + fileName + "\"")
                .body(resource);
    }
}
