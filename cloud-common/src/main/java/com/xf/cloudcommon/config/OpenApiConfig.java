package com.xf.cloudcommon.config;


import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;

import java.util.List;

/**
 * OpenApiConfig
 *
 * @author 海言
 * @date 2025/9/10
 * @time 11:36
 * @Description springdoc文档接口签字指向网关配置类
 */
@Configuration
public class OpenApiConfig {

    @Value("${spring.application.name}")
    private String serverName;

    private static final String SECURITY_SCHEME_NAME = "TokenAuth";

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .servers(List.of(
                        new Server().url("http://localhost:9090/" + serverName) // 这里写网关地址
                ))  // 配置全局 SecurityScheme
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME,
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.APIKEY)
                                        .in(SecurityScheme.In.HEADER)
                                        .name(HttpHeaders.AUTHORIZATION) // 请求头字段名
                        )
                )
                // 应用到所有接口
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME));
    }

}
