package com.example.elkmonitoring.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Spring MVC 설정
 * 로깅 인터셉터를 등록하여 모든 HTTP 요청을 추적
 */
@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final LoggingInterceptor loggingInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loggingInterceptor)
                .addPathPatterns("/**")  // 모든 경로에 적용
                .excludePathPatterns(    // 제외할 경로
                        "/actuator/**",
                        "/health",
                        "/favicon.ico"
                );
    }
}
