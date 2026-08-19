package com.miaohome.controller;

import com.miaohome.dto.ApiResult;
import com.miaohome.entity.Tenant;
import com.miaohome.repository.TenantRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 租户（小区）管理接口
 */
@Tag(name = "租户管理", description = "租户（小区）的增删查")
@RestController
@RequestMapping("/tenants")
public class TenantController {

    private final TenantRepository tenantRepository;

    public TenantController(TenantRepository tenantRepository) {
        this.tenantRepository = tenantRepository;
    }

    /** 获取所有租户列表 */
    @Operation(summary = "获取所有租户列表")
    @GetMapping("/list")
    public ApiResult<List<Tenant>> list() {
        return ApiResult.success(tenantRepository.findAll());
    }

    /** 创建新租户（小区） */
    @Operation(summary = "创建新租户")
    @PostMapping(consumes = "application/json")
    public ApiResult<Tenant> create(@Parameter(description = "租户信息") @RequestBody Tenant tenant) {
        tenant.setCreatedAt(LocalDateTime.now());
        return ApiResult.success(tenantRepository.save(tenant));
    }

    /** 删除租户 */
    @Operation(summary = "删除租户")
    @DeleteMapping("/{id}")
    public ApiResult<Void> delete(@Parameter(description = "租户 ID") @PathVariable Long id) {
        tenantRepository.deleteById(id);
        return ApiResult.success(null);
    }
}
