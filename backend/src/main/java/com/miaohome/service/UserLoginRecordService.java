package com.miaohome.service;

import com.miaohome.entity.UserLoginRecord;
import com.miaohome.repository.UserLoginRecordRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 用户登录记录业务
 */
@Service
@Transactional
public class UserLoginRecordService {

    private final UserLoginRecordRepository repository;

    public UserLoginRecordService(UserLoginRecordRepository repository) {
        this.repository = repository;
    }


    /**
     * 记录用户最近一次登录时间（upsert）
     */
    public void recordLogin(Long userId) {
        UserLoginRecord record = repository.findById(userId)
                .orElseGet(() -> {
                    UserLoginRecord newRecord = new UserLoginRecord();
                    newRecord.setUserId(userId);
                    return newRecord;
                });
        record.setLastLoginAt(LocalDateTime.now());
        record.setUpdatedAt(LocalDateTime.now());
        repository.save(record);
    }
}
