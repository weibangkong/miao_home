package com.miaohome.repository;

import com.miaohome.entity.UserLoginRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 用户登录记录数据访问
 */
@Repository
public interface UserLoginRecordRepository extends JpaRepository<UserLoginRecord, Long> {
}
