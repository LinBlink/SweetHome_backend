package asia.sweethome.location.service;

import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

import asia.sweethome.location.entity.dto.CreateFenceDTO;
import asia.sweethome.location.entity.po.Fence;
import asia.sweethome.location.entity.vo.FenceVO;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author LocrianFifth
 * @since 2026-07-14
 */
public interface IFenceService extends IService<Fence> {

    void createFence(Long userId, CreateFenceDTO dto);

    void deleteFence(Long userId, Long fenceId);

    List<FenceVO> listFamilyFences(Long userId);

}
