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
    private static final com.fasterxml.jackson.databind.ObjectMapper MAPPER = new com.fasterxml.jackson.databind.ObjectMapper();

    @Resource
    private RedisTemplate redisTemplate;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 计时开始
        long startTime = System.currentTimeMillis();
        // 1. 【2026-03-14新增】第一时间生成 TraceId
        String traceId = UUID.randomUUID().toString().replace("-", "");

        // 2. 【2026-03-14新增】将 TraceId 放入响应头，让前端在浏览器控制台能看到
        // 采用 beforeCommit 机制及 try-catch，防止 Spring Security 等 Filter 链产生 ReadOnlyHttpHeaders 导致 500 崩溃
        exchange.getResponse().beforeCommit(() -> {
            try {
                exchange.getResponse().getHeaders().set(TRACE_ID_HEADER, traceId);
            } catch (Exception e) {
                log.debug("无法写入 TraceId 到响应头：{}", e.getMessage());
            }
            return Mono.empty();
        });

        ServerHttpRequest request = exchange.getRequest();
        String requestURI = request.getURI().getPath();

        // 应对 Spring Security 6.3.4+ 引起的 StrictFirewallHttpHeaders 只读 Bug
        // 显式克隆出一个完全可写的 HttpHeaders，并使用 Decorator 包装原始 request，确保 request.mutate() 不会抛出 UnsupportedOperationException
        HttpHeaders writableHeaders = new HttpHeaders();
        try {
            writableHeaders.addAll(request.getHeaders());
        } catch (Exception e) {
            log.warn("拷贝请求头异常：{}", e.getMessage());
        }

        ServerHttpRequest decoratedRequest = new ServerHttpRequestDecorator(request) {
            @Override
            public HttpHeaders getHeaders() {
                return writableHeaders;
            }
        };

        // 3. 封装请求修改逻辑（标准 WebFlux 做法）
        ServerHttpRequest.Builder requestBuilder = decoratedRequest.mutate()
                .header("X-Internal-Auth", SECRET_KEY)
                .header(TRACE_ID_HEADER, traceId);

        // --- 1. 白名单逻辑 ---
        if (isWhiteList(requestURI)) {
            return this.successResponse(exchange, chain, traceId, null, requestBuilder.build(), startTime);
        }

        // --- 2. 获取 Token 逻辑 ---
        String token = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (token == null || !token.startsWith("Bearer")) {
            // 返回格式统一对齐 RetObj 的 {"code", "message", "data"} 字段
            return errorResponse(exchange, "{\"code\":401,\"message\":\"请先登录\"}", HttpStatus.UNAUTHORIZED);
        }

        // --- 3. 校验 Token 逻辑 ---
        String finalToken = token.startsWith("Bearer") ? token.substring(7) : token;
        String key = "alibaba-token:" + finalToken;

        // 【优化】避免在 Netty 的 EventLoop 线程中执行阻塞的 Redis 查询
        return Mono.fromCallable(() -> (String) redisTemplate.opsForValue().get(key))
                .subscribeOn(reactor.core.scheduler.Schedulers.boundedElastic())
                .flatMap(userInfoJson -> {
                    // --- 4. 最终逻辑：添加用户信息并放行 ---
                    String base64 = Base64.getEncoder().encodeToString(userInfoJson.getBytes(StandardCharsets.UTF_8));
                    requestBuilder.header("X-UserInfo", base64);
                    
                    String userId = extractUserId(userInfoJson);
                    return this.successResponse(exchange, chain, traceId, userId, requestBuilder.build(), startTime);
                })
                // 如果 Mono.fromCallable 返回 null（即 Redis 中没有此 token），会发出 empty 信号，在此处拦截并报错
                .switchIfEmpty(Mono.defer(() -> errorResponse(exchange, "{\"code\":500,\"message\":\"登录token无效或已过期\"}", HttpStatus.INTERNAL_SERVER_ERROR)));
    }

    private boolean isWhiteList(String requestURI) {
        return EXCLUDE_PATH_LIST.stream().anyMatch(requestURI::startsWith) ||
                requestURI.contains("/v3/api-docs") ||
                requestURI.contains("/doc.html");
    }

    private String extractUserId(String userInfoJson) {
        try {
            com.fasterxml.jackson.databind.JsonNode jsonNode = MAPPER.readTree(userInfoJson);
            if (jsonNode.has("id")) {
                return jsonNode.get("id").asText();
            }
        } catch (Exception e) {
            log.error("解析用户信息异常", e);
        }
        return null;
    }

    /**
     * 成功响应
     *
     * @param exchange
     * @return
     */
    private Mono<Void> successResponse(ServerWebExchange exchange, GatewayFilterChain chain,
                                       String traceId, String userId, ServerHttpRequest mutatedRequest,
                                       long startTime) {
        return chain.filter(exchange.mutate().request(mutatedRequest).build())
                .doFinally(signalType -> {
                    // 计算耗时
                    long duration = System.currentTimeMillis() - startTime;
                    // 获取响应状态码
                    HttpStatus statusCode = (HttpStatus) exchange.getResponse().getStatusCode();

                    // 将变量放入 MDC，便于 LogstashEncoder 提取为独立 JSON 字段
                    MDC.put("traceId", traceId);
                    
                    String ip = exchange.getRequest().getRemoteAddress() != null ? 
                            exchange.getRequest().getRemoteAddress().getAddress().getHostAddress() : "unknown";
                    String method = exchange.getRequest().getMethod().name();
                    String path = exchange.getRequest().getURI().getPath();
                    int status = statusCode != null ? statusCode.value() : -1;
                    
                    if (StringUtils.hasText(userId)) {
                        MDC.put("userId", userId);
                        log.info("[Access] IP: {}, Method: {}, Path: {}, Status: {}, Time: {}ms, TraceId: {}, UserId: {}",
                                ip, method, path, status, duration, traceId, userId);
                        MDC.remove("userId");
                    } else {
                        log.info("[Access] IP: {}, Method: {}, Path: {}, Status: {}, Time: {}ms, TraceId: {}",
                                ip, method, path, status, duration, traceId);
                    }

                    // 清理当前线程 MDC
                    MDC.remove("traceId");
                });
    }

    // 抽离错误返回方法，保证代码整洁
    private Mono<Void> errorResponse(ServerWebExchange exchange, String body, HttpStatus status) {
        exchange.getResponse().setStatusCode(status);
        try {
            exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        } catch (Exception e) {
            log.debug("无法设置 Content-Type：{}", e.getMessage());
        }
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);
        return exchange.getResponse().writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        return -100;
    }
}