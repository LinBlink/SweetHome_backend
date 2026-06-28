package asia.sweethome.health.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import asia.sweethome.api.FamilyApi;
import asia.sweethome.common.exception.BusinessException;
import asia.sweethome.common.exception.ErrorCode;
import asia.sweethome.health.constant.HealthMetricConstant;
import asia.sweethome.health.entity.dto.HealthRecordDTO;
import asia.sweethome.health.entity.po.HealthRecord;
import asia.sweethome.health.entity.vo.HealthRecordVO;
import asia.sweethome.health.mapper.HealthRecordsMapper;
import asia.sweethome.health.service.IHealthRecordsService;
import asia.sweethome.health.service.IHealthVisibilityService;
import lombok.RequiredArgsConstructor;

/**
 * @author LocrianFifth
 * @since 2026-07-19
 */
@Service
@RequiredArgsConstructor
public class HealthRecordsServiceImpl extends ServiceImpl<HealthRecordsMapper, HealthRecord> implements IHealthRecordsService {

    private static final int DEFAULT_PAGE_SIZE = 30;
    private static final int MAX_PAGE_SIZE = 100;

    @DubboReference
    private FamilyApi familyApi;

    private final IHealthVisibilityService healthVisibilityService;

    @Override
    public HealthRecordVO submitRecord(Long userId, HealthRecordDTO dto) {

        String metricType = dto.getMetricType();
        if (!HealthMetricConstant.TYPE_LIST.contains(metricType)) {
            throw new BusinessException(ErrorCode.INVALID_HEALTH_METRIC_TYPE);
        }

        validateValue(metricType, dto.getValue(), dto.getValueSecondary());

        LocalDate recordedAt = dto.getRecordedAt() != null ? dto.getRecordedAt() : LocalDate.now();

        Long familyId = familyApi.getFamilyByUserId(userId).getId();

        // (userId, metricType, recordedAt) 唯一：同一天同一指标重复提交是覆盖更新，不是追加
        HealthRecord one = lambdaQuery()
                .eq(HealthRecord::getUserId, userId)
                .eq(HealthRecord::getMetricType, metricType)
                .eq(HealthRecord::getRecordedAt, recordedAt)
                .one();

        if (one != null) {
            one.setValue(dto.getValue());
            one.setValueSecondary(dto.getValueSecondary());
            updateById(one);
            return toVO(one);
        }

        HealthRecord record = new HealthRecord();
        record.setUserId(userId);
        record.setFamilyId(familyId);
        record.setMetricType(metricType);
        record.setValue(dto.getValue());
        record.setValueSecondary(dto.getValueSecondary());
        record.setRecordedAt(recordedAt);
        record.setCreatedAt(LocalDateTime.now());
        save(record);

        return toVO(record);
    }

    @Override
    public HealthRecordVO updateRecord(Long userId, Long recordId, HealthRecordDTO dto) {

        HealthRecord record = getById(recordId);
        if (record == null || record.getDeletedAt() != null) {
            throw new BusinessException(ErrorCode.NO_SUCH_HEALTH_RECORD);
        }
        if (!userId.equals(record.getUserId())) {
            throw new BusinessException(ErrorCode.NOT_HEALTH_RECORD_OWNER);
        }

        // metricType 不允许改：改类型等于把这条记录变成另一个指标序列的一部分，
        // 想改类型应该是「删掉重记」，不是「编辑」，这里始终按记录原本的 metricType 校验
        validateValue(record.getMetricType(), dto.getValue(), dto.getValueSecondary());

        LocalDate newRecordedAt = dto.getRecordedAt() != null ? dto.getRecordedAt() : record.getRecordedAt();

        if (!newRecordedAt.equals(record.getRecordedAt())) {
            HealthRecord conflict = lambdaQuery()
                    .eq(HealthRecord::getUserId, userId)
                    .eq(HealthRecord::getMetricType, record.getMetricType())
                    .eq(HealthRecord::getRecordedAt, newRecordedAt)
                    .ne(HealthRecord::getId, recordId)
                    .one();
            if (conflict != null) {
                throw new BusinessException(ErrorCode.HEALTH_RECORD_DATE_CONFLICT);
            }
        }

        record.setValue(dto.getValue());
        record.setValueSecondary(dto.getValueSecondary());
        record.setRecordedAt(newRecordedAt);
        updateById(record);

        return toVO(record);
    }

    private void validateValue(String metricType, BigDecimal value, BigDecimal valueSecondary) {

        if (value == null) {
            throw new BusinessException(ErrorCode.HEALTH_RECORD_VALUE_INVALID);
        }

        // 血压必须同时有收缩压和舒张压；身高体重不应该带舒张压这个字段
        boolean isBloodPressure = HealthMetricConstant.BLOOD_PRESSURE.equals(metricType);
        if (isBloodPressure && valueSecondary == null) {
            throw new BusinessException(ErrorCode.HEALTH_RECORD_VALUE_INVALID);
        }
        if (!isBloodPressure && valueSecondary != null) {
            throw new BusinessException(ErrorCode.HEALTH_RECORD_VALUE_INVALID);
        }
    }

    @Override
    public List<HealthRecordVO> queryMyRecords(Long userId, String metricType, LocalDate from, LocalDate to, Integer page, Integer pageSize) {

        int offset = resolveOffset(page, pageSize);
        int size = resolveSize(pageSize);

        List<HealthRecord> records = lambdaQuery()
                .eq(HealthRecord::getUserId, userId)
                .eq(metricType != null, HealthRecord::getMetricType, metricType)
                .ge(from != null, HealthRecord::getRecordedAt, from)
                .le(to != null, HealthRecord::getRecordedAt, to)
                .orderByDesc(HealthRecord::getRecordedAt)
                .last("LIMIT " + size + " OFFSET " + offset)
                .list();

        return toVOList(records);
    }

    @Override
    public List<HealthRecordVO> queryFamilyMemberRecords(Long viewerId, Long memberId, String metricType, LocalDate from, LocalDate to, Integer page, Integer pageSize) {

        Long viewerFamilyId = familyApi.getFamilyByUserId(viewerId).getId();
        Long memberFamilyId = familyApi.getFamilyByUserId(memberId).getId();

        if (viewerFamilyId == null || !viewerFamilyId.equals(memberFamilyId)) {
            throw new BusinessException(ErrorCode.NOT_SAME_FAMILY);
        }

        int offset = resolveOffset(page, pageSize);
        int size = resolveSize(pageSize);

        // 显式传了某个指标类型：对方没公开就直接返回空列表，不报错——报错等于告诉查询者「对方有记录只是不给你看」，反而泄露信息
        if (metricType != null) {
            if (!HealthMetricConstant.TYPE_LIST.contains(metricType)) {
                throw new BusinessException(ErrorCode.INVALID_HEALTH_METRIC_TYPE);
            }
            if (!healthVisibilityService.isVisible(memberId, metricType)) {
                return List.of();
            }

            List<HealthRecord> records = lambdaQuery()
                    .eq(HealthRecord::getUserId, memberId)
                    .eq(HealthRecord::getMetricType, metricType)
                    .ge(from != null, HealthRecord::getRecordedAt, from)
                    .le(to != null, HealthRecord::getRecordedAt, to)
                    .orderByDesc(HealthRecord::getRecordedAt)
                    .last("LIMIT " + size + " OFFSET " + offset)
                    .list();

            return toVOList(records);
        }

        // 没传具体类型：只查对方公开了的那些类型
        List<String> visibleTypes = new ArrayList<>();
        for (String type : HealthMetricConstant.TYPE_LIST) {
            if (healthVisibilityService.isVisible(memberId, type)) {
                visibleTypes.add(type);
            }
        }

        if (visibleTypes.isEmpty()) {
            return List.of();
        }

        List<HealthRecord> records = lambdaQuery()
                .eq(HealthRecord::getUserId, memberId)
                .in(HealthRecord::getMetricType, visibleTypes)
                .ge(from != null, HealthRecord::getRecordedAt, from)
                .le(to != null, HealthRecord::getRecordedAt, to)
                .orderByDesc(HealthRecord::getRecordedAt)
                .last("LIMIT " + size + " OFFSET " + offset)
                .list();

        return toVOList(records);
    }

    private int resolveSize(Integer pageSize) {
        return (pageSize == null || pageSize < 1) ? DEFAULT_PAGE_SIZE : Math.min(pageSize, MAX_PAGE_SIZE);
    }

    private int resolveOffset(Integer page, Integer pageSize) {
        int pageNum = (page == null || page < 1) ? 1 : page;
        return (pageNum - 1) * resolveSize(pageSize);
    }

    private List<HealthRecordVO> toVOList(List<HealthRecord> records) {
        List<HealthRecordVO> result = new ArrayList<>(records.size());
        for (HealthRecord record : records) {
            result.add(toVO(record));
        }
        return result;
    }

    private HealthRecordVO toVO(HealthRecord record) {
        HealthRecordVO vo = new HealthRecordVO();
        vo.setId(record.getId());
        vo.setUserId(record.getUserId());
        vo.setMetricType(record.getMetricType());
        vo.setValue(record.getValue());
        vo.setValueSecondary(record.getValueSecondary());
        vo.setRecordedAt(record.getRecordedAt());
        return vo;
    }

}
