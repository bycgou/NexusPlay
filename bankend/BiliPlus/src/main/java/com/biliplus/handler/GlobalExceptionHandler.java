package com.biliplus.handler;

import com.biliplus.exception.BusinessException;
import com.biliplus.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.HashMap;
import java.util.Map;

/**
 * 全局异常处理：业务文案可控，禁止把内部堆栈/类名返回给客户端
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** 业务异常：透出约定文案 */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Result<?>> handleBusinessException(BusinessException e) {
        log.warn("业务异常: {}", e.getMessage());
        String msg = e.getMessage() == null ? "请求失败" : e.getMessage();
        if (msg.contains("尝试次数过多") || msg.contains("过于频繁")) {
            return ResponseEntity.status(429).body(Result.error(msg));
        }
        if (msg.contains("请先登录") || msg.contains("已被封禁")) {
            return ResponseEntity.status(401).body(Result.error(msg));
        }
        return ResponseEntity.badRequest().body(Result.error(msg));
    }

    /** 其它运行时异常：不透出 e.getMessage()，避免 NPE 等泄露类名 */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Result<?>> handleRuntimeException(RuntimeException e) {
        if (e instanceof BusinessException) {
            return handleBusinessException((BusinessException) e);
        }
        log.error("运行时异常", e);
        String m = e.getMessage() == null ? "" : e.getMessage();
        if (m.contains("请先登录")) {
            return ResponseEntity.status(401).body(Result.error("请先登录"));
        }
        return ResponseEntity.badRequest().body(Result.error("请求处理失败，请稍后再试"));
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Result<?>> handleMethodNotSupported(HttpRequestMethodNotSupportedException e) {
        return ResponseEntity.status(405).body(Result.error("请求方法不支持"));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Result<?>> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        log.warn("参数类型错误: {}", e.getName());
        return ResponseEntity.badRequest().body(Result.error("请求参数格式错误"));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Result<Map<String, String>>> handleValidationException(
            MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            if (error instanceof FieldError fieldError) {
                errors.put(fieldError.getField(), fieldError.getDefaultMessage());
            }
        });
        log.warn("参数校验失败: {}", errors);
        return ResponseEntity.badRequest().body(Result.error("请求参数错误"));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Result<?>> handleJsonError(HttpMessageNotReadableException e) {
        log.warn("JSON 解析失败: {}", e.getMessage());
        return ResponseEntity.badRequest().body(Result.error("请求参数格式错误"));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Result<?>> handleNotFound(NoResourceFoundException e) {
        return ResponseEntity.status(404).body(Result.error("资源不存在"));
    }

    @ExceptionHandler(org.apache.catalina.connector.ClientAbortException.class)
    public void handleClientAbort(org.apache.catalina.connector.ClientAbortException e) {
        log.debug("客户端中断连接: {}", e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<?>> handleGenericException(Exception e) {
        log.error("系统内部错误", e);
        return ResponseEntity.internalServerError()
                .body(Result.error("服务器内部错误，请稍后再试"));
    }
}
