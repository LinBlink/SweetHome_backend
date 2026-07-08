package asia.sweethome.moment.service.impl;

import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import asia.sweethome.api.FamilyApi;
import asia.sweethome.api.UserApi;
import asia.sweethome.api.entity.dto.UserDTO;
import asia.sweethome.common.exception.BusinessException;
import asia.sweethome.common.exception.ErrorCode;
import asia.sweethome.moment.constant.MomentMediaConstant;
import asia.sweethome.moment.entity.dto.MomentMediaDTO;
import asia.sweethome.moment.entity.dto.PostMomentDTO;
import asia.sweethome.moment.entity.po.Moment;
import asia.sweethome.moment.entity.po.MomentMedia;
import asia.sweethome.api.entity.dto.FamilyDTO;
import asia.sweethome.moment.entity.vo.MomentMediaVO;
import asia.sweethome.moment.entity.vo.MomentVO;
import asia.sweethome.moment.entity.vo.PublicMomentVO;
import asia.sweethome.moment.entity.vo.QueryMyFamilyMomentVO;
import asia.sweethome.moment.entity.vo.QueryPublicMomentVO;
import asia.sweethome.moment.mapper.MomentMapper;
import asia.sweethome.moment.service.IMomentMediaService;
import asia.sweethome.moment.service.IMomentService;
import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author LocrianFifth
 * @since 2026-07-15
 */
@Service
@RequiredArgsConstructor
public class MomentServiceImpl extends ServiceImpl<MomentMapper, Moment> implements IMomentService {

    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final int MAX_PAGE_SIZE = 50;

    @DubboReference
    private FamilyApi familyApi;

    @DubboReference
    private UserApi userApi;

    private final IMomentMediaService momentMediaService;

    @Override
    @Transactional
    public void postMoment(Long userId, PostMomentDTO dto) {

        // 发表动态的逻辑：
        /*
         *  1 创建 Moment
         *  2 save Moment
         *  3 创建关联的 MomentMedia
         *  4 一个个 save 相关的 media
         */

        // content 和 media 不能同时为空，否则会发出一条完全空白的动态
        if (
                StrUtil.isBlank(dto.getContent())
                        &&
                        (
                                dto.getMedia() == null
                                        ||
                                        dto.getMedia().isEmpty()
                        )
        ) {
            throw new BusinessException(ErrorCode.MOMENT_CONTENT_EMPTY);
        }

        // --- processing Moment

        Moment moment = new Moment();

        LocalDateTime now = LocalDateTime.now();

        moment.setFamilyId(
                familyApi.getFamilyByUserId(userId).getId()
        );
        moment.setUserId(
                userId
        );
        moment.setContent(
                dto.getContent()
        );
        // 不传默认不公开（仅本家庭可见），永远不信任「不传等于公开」这种默认值
        moment.setIsPublic(
                dto.getIsPublic() != null && dto.getIsPublic()
        );
        moment.setCreatedAt( now );
        moment.setUpdatedAt( now );

        save(moment);

        // --- processing Media

        List<MomentMediaDTO> media = dto.getMedia();

        if (media != null && !media.isEmpty()) {

            List<MomentMedia> media2db = new ArrayList<>(media.size());

            for (MomentMediaDTO mediaDTO : media) {

                String type = mediaDTO.getType();
                if (!MomentMediaConstant.TYPE_LIST.contains(type)) {
                    throw new BusinessException(ErrorCode.INVALID_MOMENT_MEDIA_TYPE);
                }

                MomentMedia momentMedia = new MomentMedia();

                String content = mediaDTO.getContent();

                momentMedia.setContent(content);
                momentMedia.setType(type);
                momentMedia.setMomentId(moment.getId());
                momentMedia.setCreatedAt(now);

                media2db.add(momentMedia);

            }

            momentMediaService.saveBatch(media2db);

        }


    }

    @Override
    public QueryMyFamilyMomentVO queryMyFamilyMoment(Long userId, Integer page, Integer pageSize, Boolean asc) {

        // page/pageSize 允许不传：page 缺省或非法时取第 1 页，pageSize 缺省或非法时取默认值，超过上限则截到上限
        int pageNum = (page == null || page < 1) ? 1 : page;
        int size = (pageSize == null || pageSize < 1) ? DEFAULT_PAGE_SIZE : Math.min(pageSize, MAX_PAGE_SIZE);

        Long familyId = familyApi.getFamilyByUserId(userId).getId();

        Page<Moment> pageSet = new Page<>(pageNum, size);

        if (asc==null) {
            asc = false;
        }

        pageSet.addOrder(
                asc
                        ? OrderItem.asc("created_at")
                        : OrderItem.desc("created_at")
        );

        Page<Moment> momentPage = lambdaQuery().eq(
                Moment::getFamilyId, familyId
        ).page(
                pageSet
        );

        // --- total

        QueryMyFamilyMomentVO vo = new QueryMyFamilyMomentVO();

        vo.setTotal(
                momentPage.getTotal()
        );

        // --- moments

        List<Moment> momentRecords = momentPage.getRecords();

        List<MomentVO> momentVOS = new ArrayList<>(momentRecords.size());

        // 批量获得用户 userDTO
        List<Long> userIds = new LinkedList<>();
        List<Long> momentIds = new LinkedList<>();

        Map<Long, UserDTO> userIdUserDTOMap = new HashMap<>();
        for (Moment momentRecord : momentRecords) {
            userIds.add(momentRecord.getUserId());
            // 顺便将 moment 的所有 id 给 momentIds
            momentIds.add(momentRecord.getId());
        }

        List<UserDTO> userDTOS = userApi.findUsersByIds(userIds);
        for (UserDTO userDTO : userDTOS) {
            userIdUserDTOMap.put(
                    userDTO.getId(),
                    userDTO
            );
        }

        // 批量获得 MomentMedias
        List<MomentMedia> momentMediaList = momentMediaService.lambdaQuery()
                .in(
                        !momentIds.isEmpty(), MomentMedia::getMomentId,
                        momentIds
                ).list();

        Map<Long, List<MomentMedia>> momentIdMomentMediaMap = momentMediaList.stream()
                .collect(
                        Collectors.groupingBy(
                                MomentMedia::getMomentId
                        )
                );


        // 取得单个Moment
        for (Moment momentRecord : momentRecords) {

            // 禁止在for中进行数据库查询

            // 拼接返回 momentVO
            MomentVO momentVO = new MomentVO();

            momentVO.setId(momentRecord.getId());
            momentVO.setUserId(momentRecord.getUserId());

            UserDTO userDTO = userIdUserDTOMap.get(
                    momentRecord.getUserId()
            );

            momentVO.setUsername(
                    userDTO.getName()
            );
            momentVO.setUserAvatarUrl(
                    userDTO.getAvatarUrl()
            );

            momentVO.setCreatedAt(momentRecord.getCreatedAt());
            momentVO.setContent(momentRecord.getContent());

            List<MomentMedia> momentMedias = momentIdMomentMediaMap.getOrDefault(momentRecord.getId(), List.of());

            List<MomentMediaVO> momentMediaVOS = new ArrayList<>(momentMedias.size());

            // 拼接返回 momentMediaVOS
            for (MomentMedia momentMedia : momentMedias) {
                MomentMediaVO momentMediaVO = new MomentMediaVO();

                momentMediaVO.setContent(momentMedia.getContent());
                momentMediaVO.setCreatedAt(momentMedia.getCreatedAt());
                momentMediaVO.setType(momentMedia.getType());

                momentMediaVOS.add(momentMediaVO);
            }

            momentVO.setMediaFiles(momentMediaVOS);

            momentVOS.add(momentVO);

        }

        vo.setMoments(momentVOS);

        return vo;

    }

    @Override
    public QueryPublicMomentVO queryPublicMoment(Integer page, Integer pageSize, Boolean asc) {

        int pageNum = (page == null || page < 1) ? 1 : page;
        int size = (pageSize == null || pageSize < 1) ? DEFAULT_PAGE_SIZE : Math.min(pageSize, MAX_PAGE_SIZE);

        Page<Moment> pageSet = new Page<>(pageNum, size);

        if (asc == null) {
            asc = false;
        }

        pageSet.addOrder(
                asc
                        ? OrderItem.asc("created_at")
                        : OrderItem.desc("created_at")
        );

        // 只按 is_public 过滤，不做家庭边界校验——这就是「公开」的意义
        Page<Moment> momentPage = lambdaQuery().eq(
                Moment::getIsPublic, true
        ).page(
                pageSet
        );

        QueryPublicMomentVO vo = new QueryPublicMomentVO();
        vo.setTotal(momentPage.getTotal());

        List<Moment> momentRecords = momentPage.getRecords();

        if (momentRecords.isEmpty()) {
            vo.setMoments(List.of());
            return vo;
        }

        // 批量查发布者用户信息、发布者所在家庭信息、关联媒体，避免循环内逐条查库（N+1）
        List<Long> userIds = new LinkedList<>();
        List<Long> familyIds = new LinkedList<>();
        List<Long> momentIds = new LinkedList<>();
        for (Moment momentRecord : momentRecords) {
            userIds.add(momentRecord.getUserId());
            familyIds.add(momentRecord.getFamilyId());
            momentIds.add(momentRecord.getId());
        }

        Map<Long, UserDTO> userIdUserDTOMap = new HashMap<>();
        for (UserDTO userDTO : userApi.findUsersByIds(userIds)) {
            userIdUserDTOMap.put(userDTO.getId(), userDTO);
        }

        Map<Long, FamilyDTO> familyIdFamilyDTOMap = new HashMap<>();
        for (FamilyDTO familyDTO : familyApi.getFamiliesByIds(familyIds)) {
            familyIdFamilyDTOMap.put(familyDTO.getId(), familyDTO);
        }

        List<MomentMedia> momentMediaList = momentMediaService.lambdaQuery()
                .in(MomentMedia::getMomentId, momentIds)
                .list();

        Map<Long, List<MomentMedia>> momentIdMomentMediaMap = momentMediaList.stream()
                .collect(Collectors.groupingBy(MomentMedia::getMomentId));

        List<PublicMomentVO> publicMomentVOS = new ArrayList<>(momentRecords.size());

        for (Moment momentRecord : momentRecords) {

            PublicMomentVO publicMomentVO = new PublicMomentVO();

            publicMomentVO.setId(momentRecord.getId());
            publicMomentVO.setUserId(momentRecord.getUserId());

            UserDTO userDTO = userIdUserDTOMap.get(momentRecord.getUserId());
            if (userDTO != null) {
                publicMomentVO.setUsername(userDTO.getName());
                publicMomentVO.setUserAvatarUrl(userDTO.getAvatarUrl());
            }

            publicMomentVO.setFamilyId(momentRecord.getFamilyId());

            FamilyDTO familyDTO = familyIdFamilyDTOMap.get(momentRecord.getFamilyId());
            if (familyDTO != null) {
                publicMomentVO.setFamilyName(familyDTO.getName());
            }

            publicMomentVO.setCreatedAt(momentRecord.getCreatedAt());
            publicMomentVO.setContent(momentRecord.getContent());

            List<MomentMedia> momentMedias = momentIdMomentMediaMap.getOrDefault(momentRecord.getId(), List.of());
            List<MomentMediaVO> momentMediaVOS = new ArrayList<>(momentMedias.size());

            for (MomentMedia momentMedia : momentMedias) {
                MomentMediaVO momentMediaVO = new MomentMediaVO();
                momentMediaVO.setContent(momentMedia.getContent());
                momentMediaVO.setCreatedAt(momentMedia.getCreatedAt());
                momentMediaVO.setType(momentMedia.getType());
                momentMediaVOS.add(momentMediaVO);
            }

            publicMomentVO.setMediaFiles(momentMediaVOS);

            publicMomentVOS.add(publicMomentVO);

        }

        vo.setMoments(publicMomentVOS);

        return vo;

    }

    @Override
    public void deleteMoment(Long userId , Long momentId) {


        Moment one = lambdaQuery().eq(
                Moment::getId,
                momentId
        ).one();

        if (one==null) {
            throw new BusinessException(
                ErrorCode.NO_SUCH_MOMENT
            );
        }

        // 只能删除自己的 moment
        if ( !userId.equals(
                one.getUserId()
        ) ){
            throw new BusinessException(
                    ErrorCode.NOT_MOMENT_OWNER
            );
        }


        removeById( one.getId() );

    }


}
