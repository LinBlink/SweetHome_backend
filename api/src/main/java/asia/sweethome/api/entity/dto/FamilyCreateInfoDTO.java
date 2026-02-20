package asia.sweethome.api.entity.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 【创建家庭的入参】
 * <p>
 * 由调用方（auth/user 服务）打包好，通过 Dubbo 传给 family-service 的 createFamily 方法。
 * 实现 Serializable 是因为要跨服务网络传输（下同）。
 *
 * @author: LOCRIAN_V
 * @date: 7/1/2026 11:05 下午
 */
@Data   // Lombok：自动生成 getter/setter 等，DTO 通常只需要它
public class FamilyCreateInfoDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    // 谁创建的这个家（会成为家庭管理员）
    private Long userId;
    // 家庭名字是什么，如「王家」
    private String familyName;
    // 创建者要加入家庭名单，需要知道性别（用于日后计算亲属称谓）
    private String gender;

}
