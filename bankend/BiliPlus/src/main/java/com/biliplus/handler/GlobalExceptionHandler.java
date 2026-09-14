package com.biliplus.handler;

import com.biliplus.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * 全局异常处理
 */

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    /**
     * 处理业务异常（如登录失败、用户不存在等）
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Result<?>> handleRuntimeException(RuntimeException e) {
        log.warn("业务异常: {}", e.getMessage(), e);
        return ResponseEntity.badRequest().body(Result.error(e.getMessage()));
    }

    /**
     * 处理参数校验异常（@Valid 失败）
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Result<Map<String, String>>> handleValidationException(
            MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String field = ((FieldError) error).getField();
            String msg = error.getDefaultMessage();
            errors.put(field, msg);
        });
        log.warn("参数校验失败: {}", errors);
        return ResponseEntity.badRequest().body(Result.error("请求参数错误"));
        // 或者更详细：return ResponseEntity.badRequest().body(new Result<>(0, "参数错误", errors));
    }

    /**
     * 视频/静态资源 Range 传输时客户端中断（拖动进度、关闭页面），无需当系统错误
     */
    @ExceptionHandler(org.apache.catalina.connector.ClientAbortException.class)
    public void handleClientAbort(org.apache.catalina.connector.ClientAbortException e) {
        log.debug("客户端中断连接: {}", e.getMessage());
    }

    /**
     * 处理所有未预期的系统异常（兜底）
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<?>> handleGenericException(Exception e) {
        log.error("系统内部错误", e);
        return ResponseEntity.internalServerError()
                .body(Result.error("服务器内部错误，请稍后再试"));
    }
}
