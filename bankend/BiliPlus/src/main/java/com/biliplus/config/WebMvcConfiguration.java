package com.biliplus.config;

import com.biliplus.interceptor.JwtTokenAdminInterceptor;
import com.biliplus.interceptor.JwtTokenPeopleInterceptor;
import com.biliplus.interceptor.ThreadLocalCleanupInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

@Slf4j
@Configuration
public class WebMvcConfiguration implements WebMvcConfigurer {

    @Autowired
    private JwtTokenAdminInterceptor jwtTokenAdminInterceptor;

    @Autowired
    private JwtTokenPeopleInterceptor jwtTokenPeopleInterceptor;

    @Autowired
    private ThreadLocalCleanupInterceptor threadLocalCleanupInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        log.info("开始注册自定义拦截器....");
        registry.addInterceptor(jwtTokenAdminInterceptor)
                .addPathPatterns("/admin/**")
                .excludePathPatterns("/admin/user/login");
        // GET 浏览公开规则在 JwtTokenPeopleInterceptor 内按方法判断
        registry.addInterceptor(jwtTokenPeopleInterceptor)
                .addPathPatterns("/pp/**")
                .excludePathPatterns(
                        "/pp/people/login",
                        "/pp/people/captcha/image",
                        "/pp/people/register",
                        "/pp/people/email",
                        "/pp/people/username"
                );

        registry.addInterceptor(threadLocalCleanupInterceptor)
                .addPathPatterns("/**");
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("http://localhost:*", "http://127.0.0.1:*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }

    @Value("${video.upload.base-path}")
    private String basePath;

    @Value("${image.upload.base-path}")
    private String imageLocalPath;

    private static final String videoAccessPrefix = "/video-files";
    private static final String imageAccessPrefix = "/images";

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String videoLoc = "file:" + new File(basePath).getAbsolutePath().replace("\\", "/") + "/";
        String imageLoc = "file:" + new File(imageLocalPath).getAbsolutePath().replace("\\", "/") + "/";

        log.info("视频资源映射: {} -> {}", videoAccessPrefix + "/**", videoLoc);
        log.info("图片资源映射: {} -> {}", imageAccessPrefix + "/**", imageLoc);

        registry.addResourceHandler(videoAccessPrefix + "/**")
                .addResourceLocations(videoLoc);

        registry.addResourceHandler(imageAccessPrefix + "/**")
                .addResourceLocations(imageLoc);
    }
}
