package com.miaohome.exception;

import com.miaohome.dto.ApiResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 * <p>捕获 Controller / Service 层抛出的各类异常，统一以 {@link ApiResult} 格式返回。</p>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /** 捕获业务异常，返回对应的业务错误码 */
    @ExceptionHandler(BusinessException.class)
    public ApiResult<Void> handleBusinessException(BusinessException ex) {
        log.warn("业务异常 [code={}] : {}", ex.getErrorCode().getCode(), ex.getMessage());
        return ApiResult.error(ex.getErrorCode().getCode(), ex.getMessage());
    }

    /** 捕获请求参数缺失（如 @RequestParam(required=true) 未传） */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ApiResult<Void> handleMissingParam(MissingServletRequestParameterException ex) {
        log.warn("请求参数缺失: {}", ex.getMessage());
        return ApiResult.error(5000, "缺少必要参数: " + ex.getParameterName());
    }

    /** 捕获请求体格式错误（如 JSON 解析失败） */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ApiResult<Void> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        log.warn("请求体格式错误: {}", ex.getMessage());
        return ApiResult.error(5000, "请求数据格式错误");
    }

    /** 捕获 IllegalArgumentException（参数校验等） */
    @ExceptionHandler(IllegalArgumentException.class)
    public ApiResult<Void> handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("参数不合法: {}", ex.getMessage());
        return ApiResult.error(5000, ex.getMessage());
    }

    /** 兜底：捕获所有未指定的异常 */
    @ExceptionHandler(Exception.class)
    public ApiResult<Void> handleException(Exception ex) {
        log.error("系统异常", ex);
        return ApiResult.error(5000, "服务器内部错误");
    }
}
