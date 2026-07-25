package asia.sweethome.api.entity.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * 【用户注册入参】
 * <p>
 * 前端注册请求 → auth-service 校验 → 通过 Dubbo 交给 user-service 建用户。
 * 家庭相关字段二选一：
 * <ul>
 *   <li>填 familyName：注册的同时新建一个家庭并当管理员；</li>
 *   <li>填 inviteCode：注册的同时凭邀请码加入已有家庭。</li>
 * </ul>
 *
 * @author: LOCRIAN_V
 * @date: 6/29/2026 10:54 PM
 */
@Data
public class UserRegisterDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String name;              // 昵称
    private String phone;             // 手机号（登录账号，唯一）
    private String password;          // 明文密码（会在后端加密后存储，不会明文落库）
    private String familyName;        // 新建家庭名（与 inviteCode 二选一）
    private String inviteCode;        // 加入家庭的邀请码（与 familyName 二选一）
    private String gender;            // 性别，male/female
    // 出生日期（ISO-8601，如 1990-03-01），选填。用于判定同辈长幼——中文里「哥哥」和「弟弟」
    // 是两个词，光知道是兄弟姐妹还不够。不填则退回 birthOrder，两者都没有时一律按年长处理。
    private LocalDate birthDate;
    private Long relationToMemberId;  // 加入家庭时，和哪位成员建立关系
    private String relationType;      // 关系类型，见 RelationTypeConstants

}
