package asia.sweethome.common.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 【统一响应结果】
 * <p>
 * 后端所有接口返回给前端的数据，都会被包装成这个 Result 对象，保证前端拿到的 JSON 结构永远一致：
 * <pre>
 * {
 *     "code": 200,          // 业务状态码：200 成功，其余见 ErrorCode
 *     "message": "success", // 给人看的提示文字
 *     "data": { ... }       // 真正的业务数据，失败时通常为 null
 * }
 * </pre>
 * 泛型 {@code <T>} 表示 data 可以是任意类型（用户信息、家庭列表、字符串……），
 * 调用时由具体业务决定，例如 {@code Result<UserInfoVO>}。
 *
 * @author: LOCRIAN_V
 * @date: 6/29/2026 10:14 PM
 */
@Data                 // Lombok：自动生成 getter/setter/toString/equals 等样板方法
@AllArgsConstructor   // Lombok：自动生成「全参构造方法」Result(code, message, data)
@NoArgsConstructor    // Lombok：自动生成「无参构造方法」，反序列化 JSON 时需要
public class Result<T> {

    /** 业务状态码，含义见 {@link asia.sweethome.common.exception.ErrorCode} */
    private int code;

    /** 提示信息，成功时一般是 "success"，失败时是错误原因 */
    private String message;

    /** 真正的业务数据，成功时有值，失败时通常为 null */
    private T data;

    /** 成功并携带数据。用法：{@code Result.success(user)} */
    public static <T> Result<T> success(T data){
        return new Result<>(200, "success", data);
    }

    /** 成功但无需返回数据（例如删除、更新操作）。用法：{@code Result.success()} */
    public static <T> Result<T> success(){
        return new Result<>(200, "success", null);
    }

    /** 失败，使用默认的 500（系统错误）状态码，只需传提示信息 */
    public static <T> Result<T> failure( String message ){
        return new Result<>(500, message, null);
    }

    /** 失败，自定义状态码 + 提示信息，是最常用的失败写法 */
    public static <T> Result<T> failure( int code, String message){
        return new Result<>(code, message, null);
    }

}
