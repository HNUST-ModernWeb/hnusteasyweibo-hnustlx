package com.hnust.lx.config;

import com.hnust.lx.interceptor.JwtTokenAdminInterceptor;
import com.hnust.lx.interceptor.JwtTokenUserInterceptor;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 配置类，注册 Web 层相关组件
 */
@Configuration
@Slf4j
@RequiredArgsConstructor
public class WebMvcConfiguration implements WebMvcConfigurer {

    private final JwtTokenAdminInterceptor jwtTokenAdminInterceptor;
    private final JwtTokenUserInterceptor jwtTokenUserInterceptor;

    /**
     * 注册自定义拦截器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        log.info("开始注册自定义拦截器...");
        
        // 管理员拦截器
        registry.addInterceptor(jwtTokenAdminInterceptor)
                .addPathPatterns("/admin/**")
                .excludePathPatterns(
                        "/admin/login",
                        "/doc.html",
                        "/webjars/**",
                        "/v3/api-docs/**",
                        "/swagger-ui/**"
                );
        
        // 用户拦截器 - 需要认证的接口
        registry.addInterceptor(jwtTokenUserInterceptor)
                .addPathPatterns(
                        "/user/info",
                        "/user/update",
                        "/user/status",
                        "/post/add",
                        "/post/delete",
                        "/post/update",
                        "/comment/add",
                        "/comment/delete",
                        "/like/toggle",
                        "/like/my-list"
                )
                .excludePathPatterns(
                        "/user/register",
                        "/user/login",
                        "/post/list",
                        "/post/detail",
                        "/comment/list",
                        "/like/post-list",
                        "/tag/**",
                        "/post-tag/**",
                        "/doc.html",
                        "/webjars/**",
                        "/v3/api-docs/**",
                        "/swagger-ui/**"
                );
    }

    /**
     * OpenAPI 3 文档信息
     */
    @Bean
    public OpenAPI openAPI() {
        log.info("准备生成接口文档...");
        return new OpenAPI()
                .info(new Info()
                        .title("微博系统接口文档")
                        .version("1.0")
                        .description("微博系统接口文档"));
    }
}