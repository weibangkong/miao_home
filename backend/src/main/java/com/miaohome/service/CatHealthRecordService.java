package com.miaohome.service;

import com.miaohome.entity.CatHealthRecord;
import com.miaohome.exception.BusinessException;
import com.miaohome.exception.ErrorCode;
import com.miaohome.repository.CatHealthRecordRepository;
import com.miaohome.repository.CatRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 猫咪健康记录业务
 * <p>管理猫咪的病史与健康检查记录。</p>
 */
@Service
@Transactional
public class CatHealthRecordService {

    private final CatHealthRecordRepository catHealthRecordRepository;
    private final CatRepository catRepository;

    public CatHealthRecordService(CatHealthRecordRepository catHealthRecordRepository,
                                  CatRepository catRepository) {
        this.catHealthRecordRepository = catHealthRecordRepository;
        this.catRepository = catRepository;
    }

    /** 获取猫咪的所有健康记录，按创建时间倒序 */
    public List<CatHealthRecord> getHealthRecords(Long catId) {
        return catHealthRecordRepository.findByCatIdOrderByCreatedAtDesc(catId);
    }

    /** 添加健康记录 */
    public CatHealthRecord addHealthRecord(Long catId, CatHealthRecord record) {
        Long tenantId = catRepository.findById(catId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CAT_NOT_FOUND, "猫咪不存在"))
                .getTenantId();
        record.setCatId(catId);
        record.setTenantId(tenantId);
        if (record.getRecordDate() == null) record.setRecordDate(LocalDate.now());
        if (record.getIsSick() == null) record.setIsSick(true);
        record.setCreatedAt(LocalDateTime.now());
        return catHealthRecordRepository.save(record);
    }

    /** 更新健康记录 */
    public CatHealthRecord updateHealthRecord(Long recordId, CatHealthRecord updated) {
        CatHealthRecord record = catHealthRecordRepository.findById(recordId)
                .orElseThrow(() -> new BusinessException(ErrorCode.HEALTH_RECORD_NOT_FOUND, "健康记录不存在"));
        if (updated.getIsSick() != null) record.setIsSick(updated.getIsSick());
        if (updated.getDiseaseName() != null) record.setDiseaseName(updated.getDiseaseName());
        if (updated.getDescription() != null) record.setDescription(updated.getDescription());
        if (updated.getTreatment() != null) record.setTreatment(updated.getTreatment());
        if (updated.getRecordDate() != null) record.setRecordDate(updated.getRecordDate());
        return catHealthRecordRepository.save(record);
    }

    /** 删除单条健康记录 */
    public void deleteHealthRecord(Long recordId) {
        CatHealthRecord record = catHealthRecordRepository.findById(recordId)
                .orElseThrow(() -> new BusinessException(ErrorCode.HEALTH_RECORD_NOT_FOUND, "健康记录不存在"));
        catHealthRecordRepository.delete(record);
    }

    /** 删除某只猫咪的全部健康记录 */
    public void deleteAllByCatId(Long catId) {
        catHealthRecordRepository.deleteByCatId(catId);
    }
}
