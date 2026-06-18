package asia.sweethome.moment.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import asia.sweethome.api.UserApi;
import asia.sweethome.api.entity.dto.UserDTO;
import asia.sweethome.common.exception.BusinessException;
import asia.sweethome.common.exception.ErrorCode;
import asia.sweethome.moment.entity.po.MomentLiker;
import asia.sweethome.moment.entity.vo.LikeUserDetailVO;
import asia.sweethome.moment.entity.vo.MomentLikeDetailsVO;
import asia.sweethome.moment.mapper.MomentLikerMapper;
import asia.sweethome.moment.service.IMomentLikerService;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author LocrianFifth
 * @since 2026-07-15
 */
@Service
public class MomentLikerServiceImpl extends ServiceImpl<MomentLikerMapper, MomentLiker> implements IMomentLikerService {

    @DubboReference
    private UserApi userApi;

    @Override
    public void likeMoment(Long momentId, Long userId) {

        baseMapper.likeMoment( momentId, userId );

    }

    @Override
    public void unlikeMoment(Long momentId, Long userId) {

        MomentLiker one = lambdaQuery().eq(
                MomentLiker::getLikerUserId, userId
        ).eq(
                MomentLiker::getMomentId, momentId
        ).one();

        if (one == null) {
            throw new BusinessException(ErrorCode.NO_SUCH_LIKE_RECORD);
        }

        removeById( one.getId() );

    }

    @Override
    public Integer getMomentLikeCount(Long momentId) {
        return baseMapper.getMomentLikeCount( momentId );
    }

    @Override
    public MomentLikeDetailsVO getMomentLikeDetails(Long momentId) {

        List<MomentLiker> likers = lambdaQuery()
                .eq(MomentLiker::getMomentId, momentId)
                .list();

        MomentLikeDetailsVO vo = new MomentLikeDetailsVO();

        if (likers.isEmpty()) {
            vo.setTotalLikes(0);
            vo.setLikers(List.of());
            return vo;
        }

        int totalLikes = likers.stream()
                .mapToInt(MomentLiker::getLikeCount)
                .sum();
        vo.setTotalLikes(totalLikes);

        // 批量查点赞者的用户信息，避免循环里一个个查 UserApi
        List<Long> likerUserIds = likers.stream()
                .map(MomentLiker::getLikerUserId)
                .collect(Collectors.toList());

        Map<Long, UserDTO> userIdUserDTOMap = userApi.findUsersByIds(likerUserIds).stream()
                .collect(Collectors.toMap(
                        UserDTO::getId, userDTO -> userDTO)
                );

        List<LikeUserDetailVO> likerVOs = likers.stream().map(liker -> {
            LikeUserDetailVO likerVO = new LikeUserDetailVO();
            likerVO.setUserId(liker.getLikerUserId());
            likerVO.setLikeCount(liker.getLikeCount());

            UserDTO userDTO = userIdUserDTOMap.get(liker.getLikerUserId());
            if (userDTO != null) {
                likerVO.setUsername(userDTO.getName());
                likerVO.setUserAvatarUrl(userDTO.getAvatarUrl());
            }

            return likerVO;
        }).collect(Collectors.toList());

        vo.setLikers(likerVOs);

        return vo;
    }

}
