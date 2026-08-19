package com.miaohome.exception;

/**
 * 业务异常
 * <p>在 Service 层抛出，由 {@link GlobalExceptionHandler} 统一捕获并转为 {@link com.miaohome.dto.ApiResult} 响应。</p>
 */
public class BusinessException extends RuntimeException {

    /** 业务错误码 */
    private final ErrorCode errorCode;

    /**
     * @param errorCode 业务错误码
     * @param message   错误描述
     */
    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    /**
     * @param errorCode 业务错误码
     * @param message   错误描述
     * @param cause     原始异常
     */
    public BusinessException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
