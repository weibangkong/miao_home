package com.miaohome.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * BCrypt 密码编码器配置
 * 提供 {@link PasswordEncoder} Bean，用于用户密码的哈希和校验。
 *
 * @author weibang kong
 */
@Configuration
public class BCryptConfig {

    /** 创建 BCrypt 密码编码器，强度因子 10 */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }
}
