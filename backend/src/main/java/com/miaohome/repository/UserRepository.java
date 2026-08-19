package com.miaohome.repository;

import com.miaohome.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 用户数据访问层
 *
 * @author weibang kong
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /** 根据手机号查询用户 */
    Optional<User> findByPhone(String phone);
}
