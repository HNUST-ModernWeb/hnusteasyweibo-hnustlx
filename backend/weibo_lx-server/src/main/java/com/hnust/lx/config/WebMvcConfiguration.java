package com.hnust.lx.config;

import com.hnust.lx.interceptor.JwtTokenAdminInterceptor;
import com.hnust.lx.interceptor.JwtTokenUserInterceptor;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class WebMvcConfiguration implements WebMvcConfigurer {

    private final JwtTokenAdminInterceptor jwtTokenAdminInterceptor;
    private final JwtTokenUserInterceptor jwtTokenUserInterceptor;

    @Value("${weibo.web.upload-path}")
    private String webUploadPath;

    @Value("${weibo.web.cors-origins:http://localhost:3000}")
    private String corsOrigins;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        String[] allowedOrigins = Arrays.stream(corsOrigins.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toArray(String[]::new);

        registry.addMapping("/**")
                .allowedOriginPatterns(allowedOrigins)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        log.info("Registering interceptors...");

        registry.addInterceptor(jwtTokenAdminInterceptor)
                .addPathPatterns("/admin/**")
                .excludePathPatterns(
                        "/admin/login",
                        "/doc.html",
                        "/webjars/**",
                        "/v3/api-docs/**",
                        "/swagger-ui/**"
                );

        registry.addInterceptor(jwtTokenUserInterceptor)
                .addPathPatterns(
                        "/user/info",
                        "/user/update",
                        "/user/status",
                        "/user/avatar",
                        "/post/add",
                        "/post/image",
                        "/post/delete",
                        "/post/update",
                        "/comment/add",
                        "/comment/delete",
                        "/comment/received",
                        "/like/toggle",
                        "/like/my-list",
                        "/like/received"
                )
                .excludePathPatterns(
                        "/user/register",
                        "/user/login",
                        "/user/info",
                        "/user/info/*",
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

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        log.info("Config resource handler, upload-path: {}", webUploadPath);
        registry.addResourceHandler("/upload/**")
                .addResourceLocations("file:" + webUploadPath);
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
