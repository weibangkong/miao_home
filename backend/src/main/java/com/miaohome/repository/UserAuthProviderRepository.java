package com.miaohome.repository;

import com.miaohome.entity.UserAuthProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 用户认证渠道数据访问层
 *
 * @author weibang kong
 */
@Repository
public interface UserAuthProviderRepository extends JpaRepository<UserAuthProvider, Long> {

    /** 按认证渠道和唯一标识查找 */
    Optional<UserAuthProvider> findByProviderAndProviderKey(String provider, String providerKey);

    /** 查找用户某类型的所有认证渠道 */
    List<UserAuthProvider> findByUserIdAndProvider(Long userId, String provider);

    /** 查找用户所有认证渠道 */
    List<UserAuthProvider> findByUserId(Long userId);
}
