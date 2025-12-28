package asia.sweethome.common.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @description: 统一响应结果
 * @author: LOCRIAN_V
 * @date: 6/29/2026 10:14 PM
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class  Result<T> {

    private int code;
    private String message;
    private T data;

    public static <T> Result<T> success(T data){
        return new Result<>(200, "success", data);
    }

    public static <T> Result<T> success(){
        return new Result<>(200, "success", null);
    }

    public static <T> Result<T> failure( String message ){
        return new Result<>(500, message, null);
    }

    public static <T> Result<T> failure( int code, String message){
        return new Result<>(code, message, null);
    }

}
