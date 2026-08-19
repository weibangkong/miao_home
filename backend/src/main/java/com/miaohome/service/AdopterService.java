package com.miaohome.service;

import com.miaohome.config.SessionContext;
import com.miaohome.entity.Adopter;
import com.miaohome.entity.Cat;
import com.miaohome.entity.Notification;
import com.miaohome.exception.BusinessException;
import com.miaohome.exception.ErrorCode;
import com.miaohome.repository.AdopterRepository;
import com.miaohome.repository.CatRepository;
import com.miaohome.repository.NotificationRepository;
import com.miaohome.tenant.TenantContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 认养业务逻辑
 */
@Service
@Transactional
public class AdopterService {

    private final AdopterRepository adopterRepository;
    private final CatRepository catRepository;
    private final NotificationRepository notificationRepository;

    public AdopterService(AdopterRepository adopterRepository,
                          CatRepository catRepository,
                          NotificationRepository notificationRepository) {
        this.adopterRepository = adopterRepository;
        this.catRepository = catRepository;
        this.notificationRepository = notificationRepository;
    }

    /** 获取当前租户下所有认养记录，按认养时间倒序；无租户时返回全部 */
    public List<Adopter> getAllAdopters() {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId != null) {
            return adopterRepository.findByTenantIdOrderByAdoptedAtDesc(tenantId);
        }
        return adopterRepository.findAllByOrderByAdoptedAtDesc();
    }

    /** 查看指定猫咪的所有认养记录 */
    public List<Adopter> getAdoptersByCat(Long catId) {
        return adopterRepository.findByCatId(catId);
    }

    /**
     * 认养猫咪
     * <ul>
     *   <li>创建认养记录</li>
     *   <li>将猫咪标记为已认养并更新楼栋</li>
     *   <li>发送认养成功通知</li>
     * </ul>
     */
    public Adopter adoptCat(Long catId, Adopter adopter) {
        Cat cat = catRepository.findById(catId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CAT_NOT_FOUND, "猫咪不存在"));

        adopter.setCatId(catId);
        adopter.setTenantId(cat.getTenantId());
        adopter.setUserId(SessionContext.isLoggedIn() ? SessionContext.getUserId() : null);
        adopter.setAdoptedAt(LocalDateTime.now());
        adopter.setIsActive(true);
        Adopter saved = adopterRepository.save(adopter);

        // 更新猫咪认养状态
        cat.setIsAdopted(true);
        catRepository.save(cat);

        // 发送认养成功通知
        Notification notification = new Notification();
        notification.setTenantId(cat.getTenantId());
        notification.setAdopterId(saved.getId());
        notification.setCatId(catId);
        notification.setTitle("猫咪认养成功");
        notification.setContent("恭喜您成功认养了 " + cat.getName() + "！认养户号：" + adopter.getHouseholdNumber());
        notification.setCreatedAt(LocalDateTime.now());
        notificationRepository.save(notification);

        return saved;
    }

    /**
     * 取消认养（软删除）
     * <ul>
     *   <li>将认养记录的 isActive 置为 false</li>
     *   <li>如果该猫咪没有其他有效认养，则恢复为未认养状态</li>
     * </ul>
     */
    public void cancelAdoption(Long id) {
        Adopter adopter = adopterRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.ADOPTER_NOT_FOUND, "认养记录不存在"));
        adopter.setIsActive(false);
        adopterRepository.save(adopter);

        // 检查猫咪是否还有其他有效认养
        Cat cat = catRepository.findById(adopter.getCatId()).orElse(null);
        if (cat != null) {
            boolean hasActiveAdopter = adopterRepository.findByCatIdAndIsActive(cat.getId(), true).isPresent();
            if (!hasActiveAdopter) {
                cat.setIsAdopted(false);
                catRepository.save(cat);
            }
        }
    }

    /** 按认养户号模糊搜索（当前租户范围内）；无租户时搜索全部 */
    public List<Adopter> searchByHousehold(String keyword) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId != null) {
            return adopterRepository.findByTenantIdAndHouseholdNumberContainingIgnoreCase(tenantId, keyword);
        }
        // 无租户时在全部数据中内存过滤
        return adopterRepository.findAllByOrderByAdoptedAtDesc().stream()
                .filter(a -> a.getHouseholdNumber() != null
                        && a.getHouseholdNumber().toLowerCase().contains(keyword.toLowerCase()))
                .toList();
    }
}
