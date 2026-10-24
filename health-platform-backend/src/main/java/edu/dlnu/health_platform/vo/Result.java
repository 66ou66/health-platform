package edu.dlnu.health_platform.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 统一返回类（约定与课程示例 dlnu_demo1 保持一致）
 * code：0 成功，1 失败；message：提示信息；data：返回数据
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Result<T> {
    private Integer code;
    private String message;
    private T data;

    //返回成功结果（带数据）
    public static <T> Result<T> success(T data) {
        return new Result<>(0, "操作成功", data);
    }

    //返回成功不带结果
    public static <T> Result<T> success() {
        return new Result<>(0, "操作成功", null);
    }

    //返回失败结果（自定义提示信息）
    public static <T> Result<T> fail(String message) {
        return new Result<>(1, message, null);
    }
}
