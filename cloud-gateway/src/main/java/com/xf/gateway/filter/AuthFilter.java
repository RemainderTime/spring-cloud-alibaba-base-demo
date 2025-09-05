package com.xf.gateway.filter;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Objects;

/**
 * @Description:
 * @ClassName: AuthFilter
 * @Author: xiongfeng
 * @Date: 2025/9/1 22:47
 * @Version: 1.0
 */
@Slf4j
@Component
public class AuthFilter implements GlobalFilter, Ordered {

    private static final List<String> EXCLUDE_PATH_LIST = Arrays.asList("/user/user/login", "/web/login", "/swagger-ui.html", "/v3/api-docs", "/swagger-ui/index.html");

    @Resource
    private RedisTemplate redisTemplate;


    private static final String SECRET_KEY = "expected-secret";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String requestURI = request.getURI().getPath();
        // 白名单直接放行
        if (EXCLUDE_PATH_LIST.stream().anyMatch(requestURI::startsWith)) {
            //重新请求头方法，并设置自定义请求头数据
            ServerHttpRequestDecorator decorator = new ServerHttpRequestDecorator(exchange.getRequest()) {
                @Override
                public HttpHeaders getHeaders() {
                    HttpHeaders headers = new HttpHeaders();
                    headers.putAll(super.getHeaders());  // 复制原始 headers
                    headers.set("X-Internal-Auth", SECRET_KEY); // 安全加 header
                    return headers;
                }
            };
            return chain.filter(exchange.mutate()
                    .request(decorator)
                    .build());
        }
        // 获取 Token
        String token = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (token == null || !token.startsWith("Bearer ")) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
        // 校验 Token
        String jwt = token.substring(7);
        log.info("是否存在token缓存-----{}",redisTemplate.hasKey("alibaba-token:" + jwt));
        String userInfoJson = (String) redisTemplate.opsForValue().get("alibaba-token:" + jwt);
        if (Objects.isNull(userInfoJson)) {
            // 登录校验失败，直接返回 JSON 响应
            String body = "{\"code\":500,\"msg\":\"请先登录\"}";
            byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
            DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);
            exchange.getResponse().setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
            exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
            return exchange.getResponse().writeWith(Mono.just(buffer));
        }
        // 修改请求头
        ServerHttpRequestDecorator decorator = new ServerHttpRequestDecorator(exchange.getRequest()) {
            @Override
            public HttpHeaders getHeaders() {
                HttpHeaders headers = new HttpHeaders();
                headers.putAll(super.getHeaders());  // 复制原始 headers
                headers.set("X-Internal-Auth", SECRET_KEY); // 安全加 header
                //Header 默认只支持 ISO-8859-1,直接放中文 JSON 会被错误解码,所以传输时加密转码UTF-8
                String base64 = Base64.getEncoder().encodeToString(userInfoJson.getBytes(StandardCharsets.UTF_8));
                headers.set("X-UserInfo", base64); // 添加用户信息
                return headers;
            }
        };
        return chain.filter(exchange.mutate()
                .request(decorator)
                .build());
    }

    @Override
    public int getOrder() {
        return -100; // 保证在最前面执行
    }

}
