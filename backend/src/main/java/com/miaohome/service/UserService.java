package com.miaohome.service;

import com.miaohome.dto.UserResponse;
import com.miaohome.entity.User;
import com.miaohome.entity.UserAuthProvider;
import com.miaohome.entity.UserType;
import com.miaohome.exception.BusinessException;
import com.miaohome.exception.ErrorCode;
import com.miaohome.repository.UserRepository;
import com.miaohome.repository.UserAuthProviderRepository;
import com.miaohome.service.file.FileStorageService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 用户业务逻辑
 * 处理手机号注册登录、微信登录、多渠道认证绑定。
 *
 * @author weibang kong
 */
@Service
@Transactional
public class UserService {

    /** 允许的头像图片扩展名 */
    private static final Set<String> ALLOWED_AVATAR_EXTENSIONS = Set.of("jpg", "jpeg", "png", "gif", "webp");

    private final UserRepository userRepo;
    private final UserAuthProviderRepository authProviderRepo;
    private final PasswordEncoder passwordEncoder;
    private final FileStorageService fileStorageService;

    public UserService(UserRepository userRepo,
                       UserAuthProviderRepository authProviderRepo,
                       PasswordEncoder passwordEncoder,
                       FileStorageService fileStorageService) {
        this.userRepo = userRepo;
        this.authProviderRepo = authProviderRepo;
        this.passwordEncoder = passwordEncoder;
        this.fileStorageService = fileStorageService;
    }

    /**
     * 注册新用户
     *
     * @param phone    手机号
     * @param password 明文密码
     * @param nickname 昵称
     * @return 创建的用户实体
     */
    public User register(String phone, String password, String nickname) {
        // 校验手机号格式
        if (phone == null || !phone.matches("^1[3-9]\\d{9}$")) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "手机号格式不正确");
        }
        // 校验密码长度
        if (password == null || password.length() < 6 || password.length() > 50) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "密码长度需在 6-50 位之间");
        }
        // 校验昵称
        if (nickname == null || nickname.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "昵称不能为空");
        }

        // 检查手机号是否已注册
        if (userRepo.findByPhone(phone).isPresent()) {
            throw new BusinessException(ErrorCode.PHONE_ALREADY_EXISTS, "该手机号已注册");
        }

        User user = new User();
        user.setPhone(phone);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setNickname(nickname.trim());
        user.setUserType(UserType.USER);
        user = userRepo.save(user);

        // 同步写入多渠道认证表
        UserAuthProvider provider = new UserAuthProvider();
        provider.setUserId(user.getId());
        provider.setProvider("phone");
        provider.setProviderKey(phone);
        provider.setCredential(user.getPasswordHash());
        authProviderRepo.save(provider);

        return user;
    }

    /**
     * 登录校验
     *
     * @param phone    手机号
     * @param password 明文密码
     * @return 登录成功的用户实体
     */
    public User login(String phone, String password) {
        if (phone == null || password == null) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS, "手机号或密码错误");
        }

        User user = userRepo.findByPhone(phone)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_CREDENTIALS, "手机号或密码错误"));

        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS, "手机号或密码错误");
        }

        return user;
    }

    /**
     * 根据 ID 查询用户
     *
     * @param userId 用户 ID
     * @return 用户实体
     */
    public User getUserById(Long userId) {
        return userRepo.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND, "用户不存在"));
    }

    /**
     * 更新用户头像（回传 OSS 对象键）
     *
     * @param userId    用户 ID
     * @param objectKey OSS 对象键（public/yyyyMMdd/uuid.ext）
     * @return 更新后的用户实体
     */
    public User updateAvatar(Long userId, String objectKey) {
        if (objectKey == null || !objectKey.matches("^public/\\d{8}/[\\w.\\-]+$")) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "非法的对象键: " + objectKey);
        }
        int dot = objectKey.lastIndexOf('.');
        String ext = dot >= 0 ? objectKey.substring(dot + 1).toLowerCase() : "";
        if (!ALLOWED_AVATAR_EXTENSIONS.contains(ext)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "头像仅支持图片格式: " + ext);
        }
        User user = getUserById(userId);
        user.setAvatarUrl(objectKey);
        return userRepo.save(user);
    }

    /**
     * 将实体转换为响应 DTO
     */
    public UserResponse toUserResponse(User user) {
        UserResponse resp = new UserResponse();
        resp.setId(user.getId());
        resp.setNickname(user.getNickname());
        resp.setAvatarUrl(fileStorageService.generateAccessUrl(user.getAvatarUrl()));
        resp.setUserType(user.getUserType());
        resp.setCreatedAt(user.getCreatedAt() != null ? user.getCreatedAt().toString() : null);
        return resp;
    }


    /**
     * 微信小程序注册或登录
     * 根据 openid 查找已有用户，没有则创建新用户。
     *
     * @param openid    微信 openid
     * @param unionid   微信 unionid（可为 null）
     * @param nickname  用户昵称（可为 null，默认"微信用户"）
     * @param avatarUrl 用户头像 URL（可为 null）
     * @return 用户实体
     */
    public User registerByWechat(String openid, String unionid, String nickname, String avatarUrl) {
        // 查 openid 是否已绑定
        return authProviderRepo.findByProviderAndProviderKey("wechat_miniapp", openid)
                .map(provider -> userRepo.findById(provider.getUserId())
                        .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND, "用户不存在")))
                .orElseGet(() -> {
                    // 新建用户
                    User user = new User();
                    user.setNickname(nickname != null && !nickname.trim().isEmpty()
                            ? nickname.trim() : "微信用户");
                    user.setAvatarUrl(avatarUrl);
                    user.setUserType(UserType.USER);
                    user = userRepo.save(user);

                    // 插入认证渠道
                    UserAuthProvider provider = new UserAuthProvider();
                    provider.setUserId(user.getId());
                    provider.setProvider("wechat_miniapp");
                    provider.setProviderKey(openid);
                    provider.setCredential(unionid);
                    authProviderRepo.save(provider);

                    return user;
                });
    }


    /**
     * 给当前用户绑定微信
     *
     * @param userId  当前登录用户 ID
     * @param openid  微信 openid
     * @param unionid 微信 unionid（可为 null）
     */
    public void bindWechat(Long userId, String openid, String unionid) {
        // 检查 openid 是否已被其他用户绑定
        authProviderRepo.findByProviderAndProviderKey("wechat_miniapp", openid).ifPresent(existing -> {
            if (!existing.getUserId().equals(userId)) {
                throw new BusinessException(ErrorCode.ALREADY_BOUND, "该微信已绑定其他账号");
            }
            throw new BusinessException(ErrorCode.ALREADY_BOUND, "当前账号已绑定微信");
        });

        // 插入绑定
        UserAuthProvider provider = new UserAuthProvider();
        provider.setUserId(userId);
        provider.setProvider("wechat_miniapp");
        provider.setProviderKey(openid);
        provider.setCredential(unionid);
        authProviderRepo.save(provider);
    }


    /**
     * 获取用户已绑定的认证渠道列表（脱敏，不返回敏感凭据）
     *
     * @param userId 用户 ID
     * @return 认证渠道列表
     */
    public List<String> getUserBindings(Long userId) {
        return authProviderRepo.findByUserId(userId).stream()
                .map(UserAuthProvider::getProvider)
                .collect(Collectors.toList());
    }


    /**
     * 创建或升级超级管理员账号
     * 手机号已存在则升级用户类型并重置密码，不存在则新建。
     *
     * @param phone    手机号
     * @param password 明文密码
     * @param nickname 昵称
     * @return 超级管理员用户实体
     */
    public User createSuperAdmin(String phone, String password, String nickname) {
        if (phone == null || !phone.matches("^1[3-9]\\d{9}$")) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "手机号格式不正确");
        }
        if (password == null || password.length() < 6) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "密码长度至少 6 位");
        }

        return userRepo.findByPhone(phone)
                .map(existing -> {
                    existing.setPasswordHash(passwordEncoder.encode(password));
                    existing.setUserType(UserType.SUPER_ADMIN);
                    if (nickname != null && !nickname.trim().isEmpty()) {
                        existing.setNickname(nickname.trim());
                    }
                    return userRepo.save(existing);
                })
                .orElseGet(() -> {
                    User user = new User();
                    user.setPhone(phone);
                    user.setPasswordHash(passwordEncoder.encode(password));
                    user.setNickname(nickname != null && !nickname.trim().isEmpty()
                            ? nickname.trim() : "超级管理员");
                    user.setUserType(UserType.SUPER_ADMIN);
                    user = userRepo.save(user);

                    UserAuthProvider provider = new UserAuthProvider();
                    provider.setUserId(user.getId());
                    provider.setProvider("phone");
                    provider.setProviderKey(phone);
                    provider.setCredential(user.getPasswordHash());
                    authProviderRepo.save(provider);

                    return user;
                });
    }
}
