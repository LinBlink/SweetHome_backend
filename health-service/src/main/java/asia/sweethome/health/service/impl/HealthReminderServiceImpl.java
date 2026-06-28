package asia.sweethome.health.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

import asia.sweethome.health.entity.dto.HealthReminderDTO;
import asia.sweethome.health.entity.po.HealthReminder;
import asia.sweethome.health.entity.vo.HealthReminderVO;
import asia.sweethome.health.mapper.HealthRemindersMapper;
import asia.sweethome.health.service.IHealthReminderService;

/**
 * @author LocrianFifth
 * @since 2026-07-19
 */
@Service
public class HealthReminderServiceImpl extends ServiceImpl<HealthRemindersMapper, HealthReminder> implements IHealthReminderService {

    @Override
    public HealthReminderVO queryMyReminder(Long userId) {

        HealthReminder one = lambdaQuery()
                .eq(HealthReminder::getUserId, userId)
                .one();

        HealthReminderVO vo = new HealthReminderVO();

        // 从未设置过是合法状态，不是错误，remindTime 留 null、enabled 给 false
        if (one == null) {
            vo.setRemindTime(null);
            vo.setEnabled(false);
            return vo;
        }

        vo.setRemindTime(one.getRemindTime());
        vo.setEnabled(Boolean.TRUE.equals(one.getEnabled()));
        return vo;
    }

    @Override
    public void updateReminder(Long userId, HealthReminderDTO dto) {

        HealthReminder one = lambdaQuery()
                .eq(HealthReminder::getUserId, userId)
                .one();

        LocalDateTime now = LocalDateTime.now();

        if (one != null) {
            one.setRemindTime(dto.getRemindTime());
            one.setEnabled(dto.getEnabled());
            one.setUpdatedAt(now);
            updateById(one);
            return;
        }

        HealthReminder reminder = new HealthReminder();
        reminder.setUserId(userId);
        reminder.setRemindTime(dto.getRemindTime());
        reminder.setEnabled(dto.getEnabled());
        reminder.setUpdatedAt(now);
        save(reminder);
    }

}
