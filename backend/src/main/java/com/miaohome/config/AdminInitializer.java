package com.miaohome.config;

import com.miaohome.entity.User;
import com.miaohome.entity.UserAuthProvider;
import com.miaohome.entity.UserType;
import com.miaohome.repository.UserAuthProviderRepository;
import com.miaohome.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 超级管理员账号初始化工具
 * 启动时通过命令行参数创建超级管理员账号。
 *
 * <pre>
 * 用法：
 *   java -jar app.jar --init-super-admin --admin-phone=13800000000 --admin-password=admin123
 *
 * 可指定昵称（可选）：
 *   java -jar app.jar --init-super-admin --admin-phone=13800000000 --admin-password=admin123 --admin-nickname=管理员
 * </pre>
 *
 * @author weibang kong
 */
@Component
public class AdminInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminInitializer.class);

    private static final String ARG_INIT_ADMIN = "--init-super-admin";
    private static final String ARG_PHONE = "--admin-phone=";
    private static final String ARG_PASSWORD = "--admin-password=";
    private static final String ARG_NICKNAME = "--admin-nickname=";

    private final UserRepository userRepo;
    private final UserAuthProviderRepository authProviderRepo;
    private final PasswordEncoder passwordEncoder;

    public AdminInitializer(UserRepository userRepo,
                            UserAuthProviderRepository authProviderRepo,
                            PasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.authProviderRepo = authProviderRepo;
        this.passwordEncoder = passwordEncoder;
    }


    @Override
    public void run(String... args) {
        boolean initAdmin = false;
        String phone = "13800000000";
        String password = "root_mht_000";
        String nickname = "超级管理员";

        for (String arg : args) {
            if (ARG_INIT_ADMIN.equals(arg)) {
                initAdmin = true;
            } else if (arg.startsWith(ARG_PHONE)) {
                phone = arg.substring(ARG_PHONE.length());
            } else if (arg.startsWith(ARG_PASSWORD)) {
                password = arg.substring(ARG_PASSWORD.length());
            } else if (arg.startsWith(ARG_NICKNAME)) {
                nickname = arg.substring(ARG_NICKNAME.length());
            }
        }

        if (!initAdmin) {
            return;
        }

        log.info("===== 超级管理员初始化开始 =====");

        // 提取为 effectively-final 变量供 lambda 使用
        final String adminPhone = phone;
        final String adminPassword = password;
        final String adminNickname = nickname;

        // 校验参数
        if (adminPhone == null || adminPhone.isBlank()) {
            log.error("缺少 --admin-phone 参数");
            printUsage();
            return;
        }
        if (adminPassword == null || adminPassword.isBlank()) {
            log.error("缺少 --admin-password 参数");
            printUsage();
            return;
        }
        if (!adminPhone.matches("^1[3-9]\\d{9}$")) {
            log.error("手机号格式不正确: {}", adminPhone);
            return;
        }
        if (adminPassword.length() < 6) {
            log.error("密码长度至少 6 位");
            return;
        }

        // 检查手机号是否已存在
        userRepo.findByPhone(adminPhone).ifPresentOrElse(
                existing -> upgradeExisting(existing, adminPassword),
                () -> createNew(adminPhone, adminPassword, adminNickname)
        );

        log.info("===== 超级管理员初始化完成 =====");
    }


    /**
     * 创建新的超级管理员账号
     */
    private void createNew(String phone, String password, String nickname) {
        String encodedPassword = passwordEncoder.encode(password);

        User user = new User();
        user.setPhone(phone);
        user.setPasswordHash(encodedPassword);
        user.setNickname(nickname);
        user.setUserType(UserType.SUPER_ADMIN);
        user = userRepo.save(user);

        // 同步写入多渠道认证表
        UserAuthProvider provider = new UserAuthProvider();
        provider.setUserId(user.getId());
        provider.setProvider("phone");
        provider.setProviderKey(phone);
        provider.setCredential(encodedPassword);
        authProviderRepo.save(provider);

        log.info("超级管理员账号创建成功:");
        log.info("  手机号: {}", phone);
        log.info("  用户ID: {}", user.getId());
        log.info("  用户类型: {} (超级管理员)", user.getUserType());
    }


    /**
     * 将已有用户升级为超级管理员（手机号已存在的情况）
     */
    private void upgradeExisting(User existing, String password) {
        String encodedPassword = passwordEncoder.encode(password);

        existing.setPasswordHash(encodedPassword);
        existing.setUserType(UserType.SUPER_ADMIN);
        userRepo.save(existing);

        // 更新或创建 phone 认证渠道
        authProviderRepo.findByUserIdAndProvider(existing.getId(), "phone")
                .stream().findFirst()
                .ifPresentOrElse(
                        provider -> {
                            provider.setCredential(encodedPassword);
                            authProviderRepo.save(provider);
                        },
                        () -> {
                            UserAuthProvider provider = new UserAuthProvider();
                            provider.setUserId(existing.getId());
                            provider.setProvider("phone");
                            provider.setProviderKey(existing.getPhone());
                            provider.setCredential(encodedPassword);
                            authProviderRepo.save(provider);
                        }
                );

        log.info("已有用户已升级为超级管理员:");
        log.info("  手机号: {}", existing.getPhone());
        log.info("  用户ID: {}", existing.getId());
        log.info("  用户类型: {} (超级管理员)", existing.getUserType());
        log.info("  密码已重置");
    }


    private void printUsage() {
        log.info("用法: java -jar app.jar --init-super-admin --admin-phone=13800000000 --admin-password=yourpassword [--admin-nickname=昵称]");
    }
}
