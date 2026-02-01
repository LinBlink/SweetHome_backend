package asia.sweethome.common.exception;

import lombok.Getter;

import java.io.Serializable;

/**
 * 【业务异常】
 * <p>
 * 当业务逻辑判定「这次请求不能继续」时（比如手机号已注册、无权限、找不到家庭），
 * 就 {@code throw new BusinessException(...)}。它继承自 RuntimeException（运行时异常），
 * 所以不需要在方法签名上写 throws，代码更简洁。
 * <p>
 * 抛出后会被 {@link GlobalExceptionHandler} 统一捕获，转换成失败的 {@link asia.sweethome.common.entity.vo.Result}
 * 返回给前端，避免把原始堆栈直接暴露给用户。
 * <p>
 * 实现 Serializable 是因为该异常会经由 Dubbo 在服务之间进行网络传输（需要被序列化）。
 *
 * @author: LOCRIAN_V
 * @date: 7/1/2026 5:40 PM
 */
@Getter   // Lombok：自动生成 getCode() 方法
public class BusinessException extends RuntimeException implements Serializable {

    // 序列化版本号：跨服务传输时用于校验类版本是否兼容
    private static final long serialVersionUID = 1L;

    /** 业务状态码，最终会写进 Result.code */
    private final int code;

    /** 常用写法：直接传一个预定义的错误码枚举，code 和 message 自动带出 */
    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());   // 把提示信息交给父类 RuntimeException 保存
        this.code = errorCode.getCode();
    }

    /** 灵活写法：手动指定状态码和提示信息（枚举里没有的一次性场景） */
    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

}
