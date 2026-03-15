package com.xf.gateway.filter;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
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
import java.util.*;

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

    private static final List<String> EXCLUDE_PATH_LIST = List.of("/cloud-user/user/login");
    private static final String SECRET_KEY = "expected-secret";
    private static final String TRACE_ID_HEADER = "traceId";

    @Resource
    private RedisTemplate redisTemplate;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 计时开始
        long startTime = System.currentTimeMillis();
        // 1. 【2026-03-14新增】第一时间生成 TraceId
        String traceId = UUID.randomUUID().toString().replace("-", "");

        // 2. 【2026-03-14新增】将 TraceId 放入响应头，让前端在浏览器控制台能看到
        exchange.getResponse().getHeaders().set(TRACE_ID_HEADER, traceId);

        ServerHttpRequest request = exchange.getRequest();
        String requestURI = request.getURI().getPath();

        // 3. 封装装饰器的逻辑
        ServerHttpRequestDecorator decorator = new ServerHttpRequestDecorator(request) {
            @Override
            public HttpHeaders getHeaders() {
                HttpHeaders headers = new HttpHeaders();
                headers.putAll(super.getHeaders());
                headers.set("X-Internal-Auth", SECRET_KEY);
                headers.set(TRACE_ID_HEADER, traceId); // 将 TraceId 传给下游微服务
                return headers;
            }
        };

        // --- 白名单逻辑 ---
        if (EXCLUDE_PATH_LIST.stream().anyMatch(requestURI::startsWith) ||
                requestURI.contains("/v3/api-docs") ||
                requestURI.contains("/doc.html")) {
            //return chain.filter(exchange.mutate().request(decorator).build());
            return this.successResponse(exchange, chain, traceId, decorator, startTime);
        }

        // --- 获取 Token 逻辑 ---
        String token = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (token == null || !token.startsWith("Bearer")) {
            // 这里返回 401，此时前端已经在 Response Header 拿到 traceId 了
            return errorResponse(exchange, "{\"code\":401,\"msg\":\"请先登录\"}", HttpStatus.UNAUTHORIZED);
        }

        // --- 校验 Token 逻辑 ---
        token = token.startsWith("Bearer") ? token.substring(7) : token;
        String key = "alibaba-token:" + token;
        String userInfoJson = (String) redisTemplate.opsForValue().get(key);

        if (Objects.isNull(userInfoJson)) {
            return errorResponse(exchange, "{\"code\":500,\"msg\":\"登录token无效或已过期\"}", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        // --- 最终逻辑：添加用户信息并放行 ---嵌套装饰
        ServerHttpRequestDecorator finalDecorator = new ServerHttpRequestDecorator(decorator) {
            @Override
            public HttpHeaders getHeaders() {
                HttpHeaders headers = super.getHeaders();
                String base64 = Base64.getEncoder().encodeToString(userInfoJson.getBytes(StandardCharsets.UTF_8));
                headers.set("X-UserInfo", base64);
                return headers;
            }
        };
//        return chain.filter(exchange.mutate().request(finalDecorator).build());
        return this.successResponse(exchange, chain, traceId, finalDecorator, startTime);
    }

    /**
     * 成功响应
     *
     * @param exchange
     * @return
     */
    private Mono<Void> successResponse(ServerWebExchange exchange, GatewayFilterChain chain,
                                       String traceId, ServerHttpRequestDecorator decorator,
                                       long startTime) {
        return chain.filter(exchange.mutate().request(decorator).build())
                .doFinally(signalType -> {
                    // 计算耗时
                    long duration = System.currentTimeMillis() - startTime;
                    // 获取响应状态码
                    HttpStatus statusCode = (HttpStatus) exchange.getResponse().getStatusCode();

                    // 打印网关访问日志
                    // 格式：[Access] IP 路径 结果 耗时 traceId
                    log.info("[Access] IP: {}, Method: {}, Path: {}, Status: {}, Time: {}ms, TraceId: {}",
                            exchange.getRequest().getRemoteAddress().getAddress().getHostAddress(),
                            exchange.getRequest().getMethod(),
                            exchange.getRequest().getURI().getPath(),
                            statusCode != null ? statusCode.value() : "unknown",
                            duration,
                            traceId);

                    // 清理当前线程 MDC
                    MDC.remove("traceId");
                });
    }

    // 抽离错误返回方法，保证代码整洁
    private Mono<Void> errorResponse(ServerWebExchange exchange, String body, HttpStatus status) {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);
        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        return exchange.getResponse().writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        return -100;
    }
}