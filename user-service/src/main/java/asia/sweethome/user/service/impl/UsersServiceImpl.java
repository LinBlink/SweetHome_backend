package asia.sweethome.user.service.impl;

import static asia.sweethome.common.constants.KafkaTopicConstants.TOPIC_USER_PROFILE_CHANGED;
import static asia.sweethome.user.constant.RedisConstants.CACHE_DOUBLE_DELETE_MS;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.benmanes.caffeine.cache.Cache;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Optional;

import asia.sweethome.api.entity.dto.UserDTO;
import asia.sweethome.common.exception.BusinessException;
import asia.sweethome.common.exception.ErrorCode;
import asia.sweethome.user.constant.RedisConstants;
import asia.sweethome.user.entity.po.OutboxMessage;
import asia.sweethome.user.entity.po.User;
import asia.sweethome.user.mapper.UsersMapper;
import asia.sweethome.user.service.IOutboxMessagesService;
import asia.sweethome.user.service.IUsersService;
import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 【用户表 服务实现类】
 * <p>
 * 继承 MyBatis-Plus 的 {@link ServiceImpl}，自带通用 CRUD；这里补两个业务方法。
 *
 * @author author
 * @since 2026-06-30
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UsersServiceImpl extends ServiceImpl<UsersMapper, User> implements IUsersService {

    private final StringRedisTemplate stringRedisTemplate;
    private final Cache<Long, Optional<UserDTO>> userDTOCache;
    private final IOutboxMessagesService outboxMessagesService;
    private final TaskScheduler taskScheduler;
    /** 按手机号查用户，查不到直接抛「用户不存在」，让调用方不必重复判空 */
    @Override
    public User findUserByPhone(String phone) {
        User one = lambdaQuery().eq(
                User::getPhone, phone
        ).one();

        if (one == null) {
            throw new BusinessException(
                    ErrorCode.USER_NOT_FOUND
            );
        }

        return one;
    }

    /**
     * 更新个人资料（昵称、头像）。采用「部分更新」策略：
     * 传了才改，没传（null / 空白）就保持原值，避免把用户没打算修改的字段冲成空。
     */
    @Transactional
    @Override
    public User updateProfile(Long userId, String name, String avatarUrl) {


        User user = getById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        // StrUtil.isNotBlank：非 null 且非纯空白才更新昵称
        if (StrUtil.isNotBlank(name)) {
            user.setName(name);
        }
        // 头像允许显式清空，所以只判 null（传空字符串视为清空头像）
        if (avatarUrl != null) {
            user.setAvatarUrl(avatarUrl);
        }
        user.setUpdatedAt(LocalDateTime.now());

        updateById(user);   // 按主键更新这条记录

        // 一旦完成数据库操作，就去执行outbox
        OutboxMessage outboxMessage = new OutboxMessage();
        outboxMessage.setTopic(TOPIC_USER_PROFILE_CHANGED);
        outboxMessage.setPayload(
                String.valueOf(userId)
        );

        outboxMessagesService.save(
                outboxMessage
        );

        // 事务提交成功后清空缓存
        // TransactionSynchronizationManager.registerSynchronization 的作用是在事务生命周期的特定节点执行自定义逻辑
        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        // L2
                        stringRedisTemplate.delete(
                                RedisConstants.userDTOCacheKey(
                                        userId
                                )
                        );

                        // L1
                        userDTOCache.invalidate( userId );

                        // todo 模拟延迟双删，日后配置主从加强
                        taskScheduler.schedule(
                                ()->{
                                    // 只清空L2，L1缓存通过 Kafka 消息清空
                                    stringRedisTemplate.delete(
                                            RedisConstants.userDTOCacheKey(
                                                    userId
                                            )
                                    );

                                    /* Kafka 消息清空 OUTBOX 已经覆盖，于是舍弃
                                    kafkaTemplate.send(
                                      TOPIC_USER_PROFILE_CHANGED,
                                      String.valueOf(userId),
                                      String.valueOf(userId)
                                    );
                                    */
                                },
                                Instant.now().plusMillis(
                                        CACHE_DOUBLE_DELETE_MS
                                )
                        );

                    }
                }
        );


        // DEPRECATED
        /*
        // 清空L2 缓存
        stringRedisTemplate
                .delete(
                        RedisConstants.userDTOCacheKey( userId )
                );

        // 清空L1 缓存
        userDTOCache
                .invalidate(userId);

        log.info("🏠 完成了user信息更新，kafka 完成消息通知，告知其他实例");
        */

        // DEPRECATED
/*        // 告知其他相同微服务实例，该user完成了更新
        kafkaTemplate.send( TOPIC_USER_PROFILE_CHANGED,
                String.valueOf(userId),
                String.valueOf(userId)).whenComplete(
                (result, ex)->{
                    if (ex != null){
                        log.error("发送用户变更消息失败，userId = {} ", userId, ex);
                    }
                }
        );*/


        return user;
    }


    /**
     * 充值接口
     * @param userId
     * @param amount
     * @return 充值是否成功
     */
    @Override
    public boolean increaseBalance(Long userId, Long amount) {
        return lambdaUpdate().eq(
                User::getId,
                userId
        ).setSql(
                amount != null && amount >0,
                "balance = balance +" + amount
        ).update();
    }

}
