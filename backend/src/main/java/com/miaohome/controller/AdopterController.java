package com.miaohome.controller;

import com.miaohome.dto.AdopterResponse;
import com.miaohome.dto.ApiResult;
import com.miaohome.entity.Adopter;
import com.miaohome.entity.Cat;
import com.miaohome.service.AdopterService;
import com.miaohome.service.CatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 认养相关接口
 */
@Tag(name = "认养管理", description = "猫咪认养记录、认养与取消认养操作")
@RestController
@RequestMapping("/adopters")
public class AdopterController {

    private final AdopterService adopterService;
    private final CatService catService;

    public AdopterController(AdopterService adopterService, CatService catService) {
        this.adopterService = adopterService;
        this.catService = catService;
    }

    /** 获取所有认养记录 */
    @Operation(summary = "获取所有认养记录")
    @GetMapping("/list")
    public ApiResult<List<AdopterResponse>> list() {
        List<Adopter> adopters = adopterService.getAllAdopters();
        List<AdopterResponse> result = adopters.stream().map(this::toAdopterResponse).collect(Collectors.toList());
        return ApiResult.success(result);
    }

    /** 获取指定猫咪的认养记录 */
    @Operation(summary = "获取指定猫咪的认养记录")
    @GetMapping("/cat/{catId}/list")
    public ApiResult<List<AdopterResponse>> getByCat(
            @Parameter(description = "猫咪 ID") @PathVariable Long catId) {
        List<Adopter> adopters = adopterService.getAdoptersByCat(catId);
        List<AdopterResponse> result = adopters.stream().map(this::toAdopterResponse).collect(Collectors.toList());
        return ApiResult.success(result);
    }

    /** 认养猫咪（创建认养记录、更新猫咪状态、发送通知） */
    @Operation(summary = "认养猫咪")
    @PostMapping(value = "/adopt/{catId}", consumes = "application/json")
    public ApiResult<AdopterResponse> adopt(
            @Parameter(description = "猫咪 ID") @PathVariable Long catId,
            @Parameter(description = "认养人信息") @RequestBody Adopter request) {
        Adopter adopter = adopterService.adoptCat(catId, request);
        return ApiResult.success(toAdopterResponse(adopter));
    }

    /** 取消认养（软删除） */
    @Operation(summary = "取消认养")
    @DeleteMapping("/{id}")
    public ApiResult<Void> cancel(@Parameter(description = "认养记录 ID") @PathVariable Long id) {
        adopterService.cancelAdoption(id);
        return ApiResult.success(null);
    }

    /** 按户号模糊搜索认养记录 */
    @Operation(summary = "按户号模糊搜索认养记录")
    @GetMapping("/search")
    public ApiResult<List<AdopterResponse>> search(
            @Parameter(description = "搜索关键字") @RequestParam String keyword) {
        List<Adopter> adopters = adopterService.searchByHousehold(keyword);
        List<AdopterResponse> result = adopters.stream().map(this::toAdopterResponse).collect(Collectors.toList());
        return ApiResult.success(result);
    }

    // ---- 内部转换方法 ----

    /** Adopter 实体 → AdopterResponse，同时查询猫咪名称 */
    private AdopterResponse toAdopterResponse(Adopter adopter) {
        AdopterResponse resp = new AdopterResponse();
        resp.setId(adopter.getId());
        resp.setCatId(adopter.getCatId());
        resp.setTenantId(adopter.getTenantId());
        resp.setUserId(adopter.getUserId());
        resp.setHouseholdNumber(adopter.getHouseholdNumber());
        resp.setAdopterName(adopter.getAdopterName());
        resp.setPhone(adopter.getPhone());
        resp.setBuilding(adopter.getBuilding());
        resp.setUnitNumber(adopter.getUnitNumber());
        resp.setAdoptedAt(adopter.getAdoptedAt() != null ? adopter.getAdoptedAt().toString() : null);
        resp.setIsActive(adopter.getIsActive());

        try {
            Cat cat = catService.getCatById(adopter.getCatId());
            resp.setCatName(cat.getName());
        } catch (Exception e) {
            resp.setCatName("未知");
        }
        return resp;
    }
}
