package asia.sweethome.location.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

import asia.sweethome.api.FamilyApi;
import asia.sweethome.api.entity.dto.FamilyDTO;
import asia.sweethome.api.entity.dto.FamilyMemberDTO;
import asia.sweethome.common.exception.BusinessException;
import asia.sweethome.common.exception.ErrorCode;
import asia.sweethome.location.entity.dto.CreateFenceDTO;
import asia.sweethome.location.entity.po.Fence;
import asia.sweethome.location.entity.vo.FenceVO;
import asia.sweethome.location.mapper.FenceMapper;
import asia.sweethome.location.service.IFenceService;
import cn.hutool.core.bean.BeanUtil;
import lombok.RequiredArgsConstructor;

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
public class FenceServiceImpl extends ServiceImpl<FenceMapper, Fence> implements IFenceService {

    @DubboReference
    private FamilyApi familyApi;


    @Override
    public void createFence(Long userId, CreateFenceDTO dto) {

        Long targetUserId = dto.getTargetUserId();
        if (targetUserId == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }

        Double fenceLng = dto.getFenceLng();
        Double fenceLat = dto.getFenceLat();
        if (fenceLng == null || fenceLat == null) {
            throw new BusinessException(ErrorCode.LOCATION_COORDINATE_INVALID);
        }

        Double fenceRange = dto.getFenceRange();
        if (fenceRange == null || fenceRange <= 0) {
            throw new BusinessException(ErrorCode.LOCATION_FENCE_RANGE_INVALID);
        }

        FamilyDTO family = familyApi.getFamilyByUserId(userId);

        // 校验 targetUserId 和设置者在同一家庭，防止给家庭外的人设置围栏
        List<FamilyMemberDTO> members = familyApi.getFamilyMembersByFamilyId(family.getId());
        boolean targetInFamily = members.stream()
                .anyMatch(m -> m.getUserId().equals(targetUserId));
        if (!targetInFamily) {
            throw new BusinessException(ErrorCode.LOCATION_TARGET_NOT_FAMILY_MEMBER);
        }

        Fence fence = new Fence();

        LocalDateTime now = LocalDateTime.now();

        fence.setName(dto.getName());
        fence.setSetterUserId(userId);
        fence.setFamilyId( family.getId() );
        fence.setTargetUserId(targetUserId);
        fence.setCreatedAt(now);
        fence.setUpdatedAt(now);
        fence.setFenceLng(fenceLng);
        fence.setFenceLat(fenceLat);
        fence.setFenceRange(fenceRange);

        save( fence );

    }

    @Override
    public void deleteFence(Long userId, Long fenceId) {

        Fence fence = getById(fenceId);
        if (fence == null) {
            throw new BusinessException(ErrorCode.NO_SUCH_FENCE);
        }

        // 只有设置者本人能删除，被监护人（targetUserId）不能删掉盯着自己的围栏
        if (!fence.getSetterUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.NOT_FENCE_SETTER);
        }

        // deletedAt 是全局逻辑删除字段，removeById 会被 MyBatis-Plus 自动改写成 UPDATE ... SET deleted_at = NOW()
        removeById(fenceId);

    }

    @Override
    public List<FenceVO> listFamilyFences(Long userId) {

        FamilyDTO family = familyApi.getFamilyByUserId(userId);

        List<Fence> fences = lambdaQuery()
                .eq(Fence::getFamilyId, family.getId())
                .list();

        return BeanUtil.copyToList(fences, FenceVO.class);

    }

}
