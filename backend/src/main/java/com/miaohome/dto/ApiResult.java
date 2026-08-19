package com.miaohome.dto;

/**
 * 统一 API 响应包装
 *
 * @param <T> 响应数据类型
 */
public class ApiResult<T> {

    /** 状态码：200 成功，其他为失败 */
    private int code;

    /** 提示消息 */
    private String message;

    /** 响应数据 */
    private T data;

    // ---- getters / setters ----

    public int getCode() { return code; }
    public void setCode(int code) { this.code = code; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public T getData() { return data; }
    public void setData(T data) { this.data = data; }

    // ---- 静态工厂方法 ----

    /** 构建成功响应 */
    public static <T> ApiResult<T> success(T data) {
        ApiResult<T> result = new ApiResult<>();
        result.code = 200;
        result.message = "success";
        result.data = data;
        return result;
    }

    /** 构建失败响应 */
    public static <T> ApiResult<T> error(int code, String message) {
        ApiResult<T> result = new ApiResult<>();
        result.code = code;
        result.message = message;
        return result;
    }
}
