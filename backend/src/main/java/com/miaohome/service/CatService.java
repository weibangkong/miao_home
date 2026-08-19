package com.miaohome.service;

import com.miaohome.entity.Cat;
import com.miaohome.exception.BusinessException;
import com.miaohome.exception.ErrorCode;
import com.miaohome.repository.CatRepository;
import com.miaohome.tenant.TenantContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 猫咪基础信息业务
 * <p>仅处理猫咪本身的增删改查，不涉及媒体或健康记录。</p>
 */
@Service
@Transactional
public class CatService {

    private final CatRepository catRepository;

    public CatService(CatRepository catRepository) {
        this.catRepository = catRepository;
    }

    /** 获取当前租户下的所有猫咪，按创建时间倒序；无租户时返回全部 */
    public List<Cat> getAllCats() {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId != null) {
            return catRepository.findByTenantIdOrderByCreatedAtDesc(tenantId);
        }
        return catRepository.findAllByOrderByCreatedAtDesc();
    }

    /** 根据 ID 获取猫咪，不存在时抛出 {@link BusinessException} */
    public Cat getCatById(Long id) {
        return catRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.CAT_NOT_FOUND, "猫咪不存在"));
    }

    /** 创建猫咪，自动填充租户 ID、时间戳、默认值；无租户时使用默认值 1 */
    public Cat createCat(Cat cat) {
        Long tenantId = TenantContext.getTenantId();
        cat.setTenantId(tenantId != null ? tenantId : 1L);
        cat.setCreatedAt(LocalDateTime.now());
        cat.setUpdatedAt(LocalDateTime.now());
        cat.setIsAdopted(false);
        if (cat.getIsNeutered() == null) cat.setIsNeutered(false);
        return catRepository.save(cat);
    }

    /** 更新猫咪信息，只覆盖非 null 字段 */
    public Cat updateCat(Long id, Cat updated) {
        Cat cat = getCatById(id);
        if (updated.getName() != null) cat.setName(updated.getName());
        if (updated.getColor() != null) cat.setColor(updated.getColor());
        if (updated.getGender() != null) cat.setGender(updated.getGender());
        if (updated.getBirthYear() != null) cat.setBirthYear(updated.getBirthYear());
        if (updated.getDescription() != null) cat.setDescription(updated.getDescription());
        if (updated.getIsAdopted() != null) cat.setIsAdopted(updated.getIsAdopted());
        if (updated.getIsNeutered() != null) cat.setIsNeutered(updated.getIsNeutered());
        if (updated.getAvatarUrl() != null) cat.setAvatarUrl(updated.getAvatarUrl());
        cat.setUpdatedAt(LocalDateTime.now());
        return catRepository.save(cat);
    }

    /** 删除猫咪（仅删除猫本身，关联数据由调用方处理） */
    public void deleteCat(Long id) {
        Cat cat = getCatById(id);
        catRepository.delete(cat);
    }
}
