package asia.sweethome.moment.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

import asia.sweethome.api.FamilyApi;
import asia.sweethome.api.UserApi;
import asia.sweethome.moment.entity.dto.MomentMediaDTO;
import asia.sweethome.moment.entity.dto.PostMomentDTO;
import asia.sweethome.moment.entity.po.Moment;
import asia.sweethome.moment.entity.po.MomentMedia;
import asia.sweethome.moment.mapper.MomentMapper;
import asia.sweethome.moment.service.IMomentMediaService;
import asia.sweethome.moment.service.IMomentService;
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


    @DubboReference
    private FamilyApi familyApi;

    @DubboReference
    private UserApi userApi;

    private final IMomentMediaService momentMediaService;

    @Override
    public void postMoment(Long userId, PostMomentDTO dto) {

        // 发表动态的逻辑：
        /*
         *  1 创建 Moment
         *  2 save Moment
         *  3 创建关联的 MomentMedia
         *  4 一个个 save 相关的 media
         */


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
        moment.setCreatedAt( now );
        moment.setUpdatedAt( now );

        save(moment);

        // --- processing Media

        // todo 我自己写。开多线程保存，加快速度

        List<MomentMediaDTO> media = dto.getMedia();

        for (MomentMediaDTO mediaDTO : media) {

            MomentMedia momentMedia = new MomentMedia();

            String type = mediaDTO.getType();
            String content = mediaDTO.getContent();

            momentMedia.setContent( content );
            momentMedia.setType( type );
            momentMedia.setMomentId( moment.getId() );
            momentMedia.setCreatedAt( now );

            momentMediaService.save(momentMedia);

        }


    }


}
