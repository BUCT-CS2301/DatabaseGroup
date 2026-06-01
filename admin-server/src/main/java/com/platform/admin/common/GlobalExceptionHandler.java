package com.platform.admin.common;

import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(BusinessException.class)
    public Result<?> handleBusinessException(BusinessException e) {
        return Result.error(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<?> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        return Result.error(ErrorCode.BAD_REQUEST, message.isBlank() ? "参数校验失败" : message);
    }

    @ExceptionHandler(BindException.class)
    public Result<?> handleBindException(BindException e) {
        String message = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        return Result.error(ErrorCode.BAD_REQUEST, message.isBlank() ? "参数校验失败" : message);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public Result<?> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException e) {
        return Result.error(ErrorCode.PAYLOAD_TOO_LARGE, "单张图片不能超过 10 MB");
    }

    @ExceptionHandler(MissingServletRequestPartException.class)
    public Result<?> handleMissingServletRequestPartException(MissingServletRequestPartException e) {
        String msg = "file".equals(e.getRequestPartName())
                ? "缺少表单字段 file"
                : "缺少必需的请求部分: " + e.getRequestPartName();
        return Result.error(ErrorCode.BAD_REQUEST, msg);
    }

    @ExceptionHandler(MultipartException.class)
    public Result<?> handleMultipartException(MultipartException e) {
        return Result.error(ErrorCode.BAD_REQUEST, "multipart 请求解析失败，请使用 multipart/form-data 上传");
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public Result<?> handleHttpMediaTypeNotSupportedException(HttpMediaTypeNotSupportedException e) {
        return Result.error(ErrorCode.BAD_REQUEST, "不支持的 Content-Type");
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public Result<?> handleNoResourceFoundException(NoResourceFoundException e) {
        return Result.error(ErrorCode.NOT_FOUND, "资源不存在");
    }

    @ExceptionHandler(Exception.class)
    public Result<?> handleException(Exception e) {
        return Result.error(ErrorCode.INTERNAL_ERROR, e.getMessage() == null ? "服务器内部错误" : e.getMessage());
    }
}
