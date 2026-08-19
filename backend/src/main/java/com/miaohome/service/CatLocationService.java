package com.miaohome.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.miaohome.dto.FrequentCommunityItem;
import com.miaohome.entity.CatLocation;
import com.miaohome.entity.CatLocationPK;
import com.miaohome.repository.CatLocationRepository;
import com.miaohome.repository.TenantRepository;
import com.miaohome.entity.Tenant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/**
 * 猫咪出没地点业务
 */
@Service
@Transactional
public class CatLocationService {

    private final CatLocationRepository repository;
    private final TenantRepository tenantRepository;
    private final ObjectMapper objectMapper;

    public CatLocationService(CatLocationRepository repository,
                               TenantRepository tenantRepository,
                               ObjectMapper objectMapper) {
        this.repository = repository;
        this.tenantRepository = tenantRepository;
        this.objectMapper = objectMapper;
    }


    /**
     * 获取某猫的出没地点列表（含小区名称）
     */
    public List<FrequentCommunityItem> getByCatIdAndTenantId(Long catId, Long tenantId) {
        List<FrequentCommunityItem> items = repository.findById(new CatLocationPK(catId, tenantId))
                .map(record -> parseBuilding(record.getBuilding()))
                .orElse(Collections.emptyList());

        // 填充小区名称
        for (FrequentCommunityItem item : items) {
            if (item.getTenantId() != null) {
                tenantRepository.findById(item.getTenantId())
                        .ifPresent(t -> item.setTenantName(t.getName()));
            }
        }
        return items;
    }

    /**
     * 创建或更新猫咪的出没地点
     * 如果 building 列表为空则删除已有记录。
     */
    public void saveOrUpdate(Long catId, Long tenantId, List<FrequentCommunityItem> items) {
        CatLocationPK pk = new CatLocationPK(catId, tenantId);

        if (CollectionUtils.isEmpty(items)) {
            repository.deleteById(pk);
            return;
        }

        CatLocation record = repository.findById(pk)
                .orElseGet(() -> {
                    CatLocation newRecord = new CatLocation();
                    newRecord.setCatId(catId);
                    newRecord.setTenantId(tenantId);
                    newRecord.setCreatedAt(LocalDateTime.now());
                    return newRecord;
                });

        record.setBuilding(toJson(items));
        record.setUpdatedAt(LocalDateTime.now());
        repository.save(record);
    }

    /**
     * 删除指定猫的出没地点记录（删除猫咪时级联调用）
     */
    public void deleteByCatIdAndTenantId(Long catId, Long tenantId) {
        repository.deleteByCatIdAndTenantId(catId, tenantId);
    }


    /** 将 JSON 字符串解析为 DTO 列表 */
    private List<FrequentCommunityItem> parseBuilding(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<List<FrequentCommunityItem>>() {});
        } catch (JsonProcessingException e) {
            return Collections.emptyList();
        }
    }

    /** 将 DTO 列表序列化为 JSON 字符串 */
    private String toJson(List<FrequentCommunityItem> items) {
        try {
            return objectMapper.writeValueAsString(items);
        } catch (JsonProcessingException e) {
            return "[]";
        }
    }
}
