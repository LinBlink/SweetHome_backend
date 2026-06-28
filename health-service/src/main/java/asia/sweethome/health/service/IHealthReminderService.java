package asia.sweethome.health.service;

import com.baomidou.mybatisplus.extension.service.IService;

import asia.sweethome.health.entity.dto.HealthReminderDTO;
import asia.sweethome.health.entity.po.HealthReminder;
import asia.sweethome.health.entity.vo.HealthReminderVO;

/**
 * @author LocrianFifth
 * @since 2026-07-19
 */
public interface IHealthReminderService extends IService<HealthReminder> {

    /**
     * 从未设置过时返回 remindTime=null、enabled=false，不是 404
     */
    HealthReminderVO queryMyReminder(Long userId);

    void updateReminder(Long userId, HealthReminderDTO dto);

}
