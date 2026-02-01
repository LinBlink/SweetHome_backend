package asia.sweethome.common.exception;

import asia.sweethome.common.entity.vo.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 【全局异常处理器】
 * <p>
 * Controller 里抛出的任何异常，都会先「冒泡」到这里被统一拦截，转换成前端能看懂的 {@link Result}。
 * 这样做的好处：业务代码只管 throw，不用到处写 try-catch；前端拿到的错误格式也永远统一。
 * <p>
 * {@code @RestControllerAdvice} 表示这是一个「全局的 Controller 增强」，对所有 Controller 生效；
 * {@code @ExceptionHandler} 标注的方法负责处理「某一类异常」。Spring 会按异常类型从最具体到最宽泛匹配。
 *
 * @author: LOCRIAN_V
 * @date: 7/1/2026 5:42 PM
 */
@Slf4j   // Lombok：自动生成名为 log 的日志对象
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理「业务异常」——这是我们主动 throw 的、可预期的错误（如手机号已注册）。
     * 用 warn 级别记录（不是严重错误），并把异常里携带的 code / message 原样返回。
     */
    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusiness(BusinessException e) {
        log.warn("业务异常, code={}, msg={}", e.getCode(), e.getMessage());
        return Result.failure(e.getCode(), e.getMessage());
    }

    /**
     * 处理「参数校验失败」——当 DTO 上的 @NotBlank / @Size 等校验注解不通过时，
     * Spring 会抛出这两类异常。这里把第一条校验错误提示提取出来返回给前端，
     * 状态码固定为 400（参数错误），而不是让它掉进下面的 500 分支。
     */
    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public Result<Void> handleValidation(BindException e) {
        FieldError fieldError = e.getBindingResult().getFieldError();
        String msg = fieldError != null ? fieldError.getDefaultMessage()
                : ErrorCode.PARAM_ERROR.getMessage();
        log.warn("参数校验失败: {}", msg);
        return Result.failure(ErrorCode.PARAM_ERROR.getCode(), msg);
    }

    /**
     * 兜底处理「其它所有未预料到的异常」（空指针、数据库报错等）。
     * 这类才是真正的 bug，用 error 级别把完整堆栈打进日志方便排查；
     * 但只给前端返回一句笼统的「系统繁忙」，避免把内部实现细节泄漏出去。
     */
    @ExceptionHandler(Exception.class)
    public Result<Void> handleUnknown(Exception e) {
        log.error("系统异常 ", e);
        return Result.failure(
                ErrorCode.SYSTEM_ERROR.getCode(),
                ErrorCode.SYSTEM_ERROR.getMessage()
        );
    }

}
