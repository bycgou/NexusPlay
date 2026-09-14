package com.biliplus.interceptor;

import com.biliplus.utils.UserContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Component
public class ThreadLocalCleanupInterceptor implements HandlerInterceptor {

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) throws Exception {
        // 清理 ThreadLocal，避免线程池复用导致内存泄漏或数据污染
        UserContext.clear();
        // 如果还有其他 ThreadLocal，也在这里清理
        log.debug("ThreadLocal 已清理");
    }
}
