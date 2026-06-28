package asia.sweethome.health.service;

import com.baomidou.mybatisplus.extension.service.IService;

import java.time.LocalDate;
import java.util.List;

import asia.sweethome.health.entity.dto.HealthRecordDTO;
import asia.sweethome.health.entity.po.HealthRecord;
import asia.sweethome.health.entity.vo.HealthRecordVO;

/**
 * @author LocrianFifth
 * @since 2026-07-19
 */
public interface IHealthRecordsService extends IService<HealthRecord> {

    /**
     * 提交一条记录。同一天同一指标已有记录时是覆盖更新，不是追加。
     */
    HealthRecordVO submitRecord(Long userId, HealthRecordDTO dto);

    /**
     * 手动修改一条已有记录（只能改自己的），metricType 不允许改，
     * recordedAt 改到跟自己另一条记录冲突时抛 HEALTH_RECORD_DATE_CONFLICT
     */
    HealthRecordVO updateRecord(Long userId, Long recordId, HealthRecordDTO dto);

    /**
     * 查自己的历史记录
     */
    List<HealthRecordVO> queryMyRecords(Long userId, String metricType, LocalDate from, LocalDate to, Integer page, Integer pageSize);

    /**
     * 查某个家庭成员的记录：校验同一家庭 + 按可见性过滤
     */
    List<HealthRecordVO> queryFamilyMemberRecords(Long viewerId, Long memberId, String metricType, LocalDate from, LocalDate to, Integer page, Integer pageSize);

}
