package asia.sweethome.location.service.impl;

import static asia.sweethome.location.constants.FenceAlarmConstants.STEPPED_INSIDE;
import static asia.sweethome.location.constants.FenceAlarmConstants.STEPPED_OUTSIDE;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import asia.sweethome.api.FamilyApi;
import asia.sweethome.api.UserApi;
import asia.sweethome.api.entity.dto.UserDTO;
import asia.sweethome.common.constants.KafkaTopicConstants;
import asia.sweethome.common.entity.ko.FenceAlarmMessageKO;
import asia.sweethome.location.entity.po.Fence;
import asia.sweethome.location.entity.po.FenceAlarm;
import asia.sweethome.location.entity.po.OutboxMessage;
import asia.sweethome.location.entity.ro.CurrentLocationRO;
import asia.sweethome.location.entity.vo.FenceAlarmVO;
import asia.sweethome.location.mapper.FenceAlarmMapper;
import asia.sweethome.location.service.IFenceAlarmService;
import asia.sweethome.location.service.IFenceService;
import asia.sweethome.location.service.IOutboxMessagesService;
import asia.sweethome.location.util.GeoUtil;
import cn.hutool.core.bean.BeanUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author LocrianFifth
 * @since 2026-07-14
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FenceAlarmServiceImpl extends ServiceImpl<FenceAlarmMapper, FenceAlarm> implements IFenceAlarmService {

    @DubboReference
    private FamilyApi familyApi;

    @DubboReference
    private UserApi userApi;

    private final IFenceService fenceService;

    private final IOutboxMessagesService outboxMessagesService;

    private final ObjectMapper objectMapper;

    /**
     * 检查是否围栏越界
     * @param targetUserId 被监视用户
     * @param previous 之前的坐标（在Redis里查最新值）
     * @param newLng  现在的坐标
     * @param newLat  现在的坐标
     */
    @Transactional
    @Override
    public void checkAndRecordCrossing(Long targetUserId, CurrentLocationRO previous, Double newLng, Double newLat) {

        if (previous==null) {
            return;
            // 没有之前数据，第一次上报，忽略
        }

        // 得到被监视用户的所有围栏
        List<Fence> fences = fenceService.lambdaQuery().eq(
                Fence::getTargetUserId, targetUserId
        ).list();

        // 得到所有围栏name
        Map<Long, String> fenceIdNameMap = fences.stream().collect(
                Collectors.toMap(
                        Fence::getId,
                        Fence::getName
                )
        );

        for (Fence fence : fences) {
            // 之前是否在围栏里
            boolean wasInside = GeoUtil.distanceMeters(
                    previous.getLng(),
                    previous.getLat(),
                    fence.getFenceLng(),
                    fence.getFenceLat()
            ) < fence.getFenceRange();

            // 现在是否在围栏里
            boolean isInside = GeoUtil.distanceMeters(
                    newLng,
                    newLat,
                    fence.getFenceLng(),
                    fence.getFenceLat()
            ) < fence.getFenceRange();

            // 状态没变化，不用报警
            if (wasInside == isInside) {
                continue;
            }

            // 状态变了
            /**
             * 之前在外面，现在在里面
             */
            boolean steppedInside = !wasInside & isInside;

            FenceAlarm fenceAlarm = new FenceAlarm();

            fenceAlarm.setFenceId(fence.getId());

            fenceAlarm.setAlarmType(
                    steppedInside ?
                            STEPPED_INSIDE : STEPPED_OUTSIDE
            );

            LocalDateTime now = LocalDateTime.now();

            fenceAlarm.setAlarmedAt( now );

            fenceAlarm.setSetterUserId(fence.getSetterUserId());

            fenceAlarm.setTargetUserId(fence.getTargetUserId());


            fenceAlarm.setFamilyId(
                    fence.getFamilyId()
            );

            // 将报警入表
            save( fenceAlarm );

            // fenceAlarm 转为 Kafka消息负载
            FenceAlarmMessageKO fenceAlarmMessageKO = BeanUtil.copyProperties(fenceAlarm, FenceAlarmMessageKO.class);

            fenceAlarmMessageKO.setFenceName(
                    fenceIdNameMap.get(fenceAlarm.getFenceId())
            );

            // 将消息存入 outbox
            // 监控该用户的用户将收到该消息
            OutboxMessage msg = new OutboxMessage();

            msg.setTopic(
                    KafkaTopicConstants.TOPIC_FENCE_ALARM_TRIGGERED
            );

            try {
                msg.setPayload(
                        objectMapper.writeValueAsString(
                                fenceAlarmMessageKO
                        )
                );
            } catch (JsonProcessingException e) {
                log.warn("ObjectMapper消息解析异常", e);
                continue;
            }

            msg.setStatus(OutboxMessage.STATUS_UNSEND);
            msg.setRetryCount(0);
            msg.setCreatedAt(LocalDateTime.now());

            outboxMessagesService.save(
                    msg
            );


        }

    }

    /**
     * 查看我收到的所有历史报警（按触发时间倒序）
     * @param userId 当前登录用户——报警只通知围栏的设置者本人，所以这里按 setter_user_id 过滤
     */
    @Override
    public List<FenceAlarmVO> listAlarms(Long userId) {

        List<FenceAlarm> alarms = lambdaQuery()
                .eq(FenceAlarm::getSetterUserId, userId)
                .orderByDesc(FenceAlarm::getAlarmedAt)
                .list();

        if (alarms.isEmpty()) {
            return List.of();
        }

        // 批量反查围栏名称：围栏可能已经被删除（逻辑删除），listByIds 查不到的就是 null，做兜底
        List<Long> fenceIds = alarms.stream()
                .map(FenceAlarm::getFenceId)
                .distinct()
                .toList();
        Map<Long, Fence> fenceById = fenceService.listByIds(fenceIds).stream()
                .collect(Collectors.toMap(Fence::getId, f -> f));

        // 批量反查被监护人信息，避免每条报警都单独打一次 Dubbo
        List<Long> targetUserIds = alarms.stream()
                .map(FenceAlarm::getTargetUserId)
                .distinct()
                .toList();
        Map<Long, UserDTO> userById = userApi.findUsersByIds(targetUserIds).stream()
                .collect(Collectors.toMap(UserDTO::getId, u -> u));

        List<FenceAlarmVO> vos = new ArrayList<>(alarms.size());
        for (FenceAlarm alarm : alarms) {

            FenceAlarmVO vo = new FenceAlarmVO();
            vo.setId(alarm.getId());
            vo.setFenceId(alarm.getFenceId());

            Fence fence = fenceById.get(alarm.getFenceId());
            vo.setFenceName(fence == null ? null : fence.getName());

            vo.setAlarmType(alarm.getAlarmType());
            vo.setAlarmedAt(alarm.getAlarmedAt());
            vo.setTargetUserId(alarm.getTargetUserId());

            UserDTO targetUser = userById.get(alarm.getTargetUserId());
            vo.setTargetUsername(targetUser == null ? null : targetUser.getName());
            vo.setTargetUserAvatarUrl(targetUser == null ? null : targetUser.getAvatarUrl());

            vos.add(vo);
        }

        return vos;
    }

}
