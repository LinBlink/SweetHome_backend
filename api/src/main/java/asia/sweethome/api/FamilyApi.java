package asia.sweethome.api;

import asia.sweethome.api.entity.dto.FamilyCreateInfoDTO;
import asia.sweethome.api.entity.dto.FamilyDTO;
import asia.sweethome.api.entity.dto.FamilyJoinInfoDTO;
import asia.sweethome.api.entity.dto.RelationDTO;
import asia.sweethome.api.entity.dto.RelationQueryDTO;

/**
 * 【family-service 对外暴露的 Dubbo 接口】
 * <p>
 * Dubbo 是一个 RPC 框架：它让「调用另一个微服务的方法」看起来和「调用本地方法」一模一样。
 * family-service 把这个接口「注册」到注册中心（Nacos），实现类在 family-service 里；
 * 其它服务（如 auth-service、user-service）只要拿到这个接口就能直接调，Dubbo 在背后
 * 帮你把参数打包、走网络、拿回结果。
 * <p>
 * 因为接口和实现分处不同服务、参数要经过网络传输，所以入参/出参 DTO 都必须实现 Serializable。
 * 本接口放在公共的 api 模块，供「提供方」和「调用方」共同依赖。
 *
 * @author: LOCRIAN_V
 * @date: 7/1/2026 11:01 下午
 */
public interface FamilyApi {

    // 创建一个新家庭（创建者自动成为管理员并入家庭名单），返回家庭信息
    FamilyDTO createFamily(
            FamilyCreateInfoDTO familyCreateInfoDTO
    );

    // 凭邀请码加入已有家庭，返回加入的家庭信息
    FamilyDTO joinFamily(
            FamilyJoinInfoDTO familyJoinInfoDTO
    );

    // 根据用户 id 查询他所在的家庭；用户不是任何家庭的成员时会抛出业务异常（NO_SUCH_FAMILY_MEMBER）
    FamilyDTO getFamilyByUserId(
            Long userId
    );

    // 根据用户 id 找到其在家庭中的角色
    String getFamilyRoleByUserId(
            Long userId
    );

    // 根据用户 id 找到其在当前家庭中的性别（写入 family_members.gender，亲属称谓计算的基础输入）
    String getGenderByUserId(
            Long userId
    );

    // 计算 viewer 相对 target 的亲属称谓，两人不在同一家庭时返回的 DTO 内字段均为 null
    RelationDTO getRelation(
            RelationQueryDTO relationQueryDTO
    );
}
