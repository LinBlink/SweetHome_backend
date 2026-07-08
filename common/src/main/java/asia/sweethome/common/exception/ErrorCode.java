package asia.sweethome.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 【错误码枚举】
 * <p>
 * 把项目里所有「失败情况」集中定义成一个个常量，每个常量绑定一对（数字状态码，中文提示）。
 * 好处：错误提示不再散落在各处硬编码，改文案只改这一处；抛异常时写 {@code new BusinessException(ErrorCode.LOGIN_FAILED)}
 * 一目了然。
 * <p>
 * code 取值遵循 doc/api.md「通用约定 · 错误码说明」的七个分类：
 * 200 成功 / 400 参数错误 / 401 未认证或过期 / 403 无权限 / 404 资源不存在 / 409 数据冲突 / 500 系统错误。
 * 下面按业务模块分组（COMMON 通用 / AUTH 认证 / FAMILY 家庭 / CHAT 聊天）。
 *
 * @author: LOCRIAN_V
 * @date: 7/1/2026 5:35 PM
 */
@Getter            // Lombok：为下面的 code、message 字段自动生成 getCode()/getMessage()
@AllArgsConstructor // Lombok：生成 (int code, String message) 构造方法，供每个枚举项括号里传参使用
public enum ErrorCode {

    // COMMON
    SUCCESS(200, "成功"),
    PARAM_ERROR(400, "参数错误"),
    UNAUTHORIZED(401, "未认证或 Token 过期"),
    FORBIDDEN(403, "无权限"),
    NOT_FOUND(404, "资源不存在"),
    CONFLICT(409, "数据冲突"),
    SYSTEM_ERROR(500, "系统繁忙，请稍后再试"),


    // FILE UPLOAD
    EMPTY_FILE(400,"上传的文件为空"),
    FILE_SIZE_ILLEGAL(400, "上传的文件大小不符合要求"),
    FILE_TYPE_ILLEGAL(400, "上传的文件类型不符合要求"),
    FILE_NAME_ILLEGAL(400, "上传的文件名不符合要求"),
    FILE_UPLOAD_ERROR(400, "文件上传时服务器繁忙，请稍后再试"),

    // AUTH
    PHONE_FORMAT_NOT_VALID(400, "手机号码不符合要求，请检查"),
    PASSWORD_FORMAT_NOT_VALID(400, "密码格式不符合要求，请检查"),
    NAME_FORMAT_NOT_VALID(400, "昵称不符合要求，请检查"),
    REGISTER_PARAM_CONFLICT(400, "familyName 与 inviteCode 必须二选一"),
    PHONE_ALREADY_EXISTS(409, "手机号已经被注册"),
    LOGIN_FAILED(401, "手机号或密码错误"),
    TOKEN_INVALID(401, "登录已失效，请重新登录"),
    TOKEN_EXPIRED(401, "登录已过期，请重新登录"),
    REFRESH_TOKEN_INVALID(401, "刷新令牌无效或已过期"),
    USER_NOT_FOUND(404, "用户不存在"),

    // FAMILY
    FAMILY_NAME_EMPTY(400, "家庭名称为空，无法创建新的家庭"),
    FAMILY_INVITE_CODE_EMPTY(400, "邀请码为空"),
    INVALID_RELATION_TYPE(400, "无效的家庭关系类型"),
    NO_SUCH_FAMILY(404, "无法找到匹配的家庭"),
    NO_SUCH_FAMILY_MEMBER(404, "无法找到该家庭成员"),
    INVITE_CODE_INVALID(404, "邀请码不存在或已过期"),
    INVALID_RELATION_ANCHOR(404, "无效的关系锚点"),
    NOT_FAMILY_MEMBER(403, "当前用户不是该家庭成员"),
    NOT_FAMILY_ADMIN(403, "仅家庭管理员可执行该操作"),
    FAMILY_SAVE_FAILURE(500, "家庭创建失败"),

    // FAMILY_RELATION
    SPOUSE_ALREADY_EXISTS(409, "配偶已经存在"),
    NO_KNOWN_PARENT(409, "未知父母，无法建立兄弟姐妹关系"),

    // CHAT
    NO_SUCH_CONVERSATION(404, "会话不存在"),
    NOT_CONVERSATION_MEMBER(403, "当前用户不是该会话成员"),
    MESSAGE_TOO_LONG(400, "消息内容超长"),
    INVALID_MESSAGE_TYPE(400, "消息类型不正确"),

    // LOCATION
    LOCATION_COORDINATE_INVALID(400, "经纬度不能为空"),
    LOCATION_BATTERY_INVALID(400, "电量数值不合法"),
    LOCATION_TIMESTAMP_MISSING(400, "定位时间戳不能为空"),
    LOCATION_TIMESTAMP_STALE(400, "定位数据已过期，请重新上报"),
    LOCATION_TARGET_NOT_FAMILY_MEMBER(403, "目标用户不是同一家庭成员"),
    LOCATION_FENCE_RANGE_INVALID(400, "围栏半径不合法"),
    NO_SUCH_FENCE(404, "围栏不存在"),
    NOT_FENCE_SETTER(403, "仅围栏设置者可执行该操作"),

    // MOMENT
    MOMENT_CONTENT_EMPTY(400, "动态内容和媒体不能同时为空"),
    INVALID_MOMENT_MEDIA_TYPE(400, "媒体类型不正确"),
    NO_SUCH_LIKE_RECORD(404, "尚未点赞，无法取消"),
    NO_SUCH_MOMENT(404, "动态不存在"),
    NOT_MOMENT_OWNER(403, "仅动态发布者本人可执行该操作"),
    COMMENT_CONTENT_EMPTY(400, "评论内容不能为空"),
    NO_SUCH_COMMENT(404, "评论不存在"),
    NOT_COMMENT_OWNER(403, "仅评论作者本人可执行该操作"),

    // HEALTH
    INVALID_HEALTH_METRIC_TYPE(400, "健康指标类型不正确"),
    HEALTH_RECORD_VALUE_INVALID(400, "健康记录数值不合法"),
    NOT_SAME_FAMILY(403, "目标用户不是同一家庭成员"),
    INVALID_REMIND_TIME(400, "提醒时间格式不正确"),
    NO_SUCH_HEALTH_RECORD(404, "健康记录不存在"),
    NOT_HEALTH_RECORD_OWNER(403, "仅本人可修改自己的健康记录"),
    HEALTH_RECORD_DATE_CONFLICT(409, "该日期已存在同指标的记录"),

    // REDPACKET
    INVALID_REDPACKET_AMOUNT(400, "红包数值太小了，改大一些"),
    INVALID_REDPACKET(404, "红包不存在或已过期"),
    REDPACKET_GRABBED_ALREADY(400, "已经抢过红包，不能再抢"),
    REDPACKET_EMPTY(400, "红包被抢完了"),
    REDPACKET_EXPIRED(400, "红包过期了"),
    REDPACKET_NUMBER_MORE_THAN_CONVERSATION_MEMBERS(400, "红包数量大于对话成员数量");


    /** HTTP 风格的数字状态码，会写进 Result.code */
    private final int code;
    /** 面向用户的中文提示，会写进 Result.message */
    private final String message;

}
