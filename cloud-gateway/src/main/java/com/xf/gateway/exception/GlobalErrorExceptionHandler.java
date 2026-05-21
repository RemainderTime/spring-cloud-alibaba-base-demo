package com.xf.gateway.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

/**
 * @Description: 网关全局异常处理器 (WebFlux)
 * @ClassName: GlobalErrorExceptionHandler
 */
@Slf4j
@Order(-1) // 优先级要高，覆盖系统默认的异常处理
@Configuration
public class GlobalErrorExceptionHandler implements ErrorWebExceptionHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        ServerHttpResponse response = exchange.getResponse();

        // 如果响应已经提交，直接结束
        if (response.isCommitted()) {
            return Mono.error(ex);
        }

        // 设置响应头
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        int code = 500;
        String message = "网关内部服务器错误";

        // 判断异常类型
        if (ex instanceof ResponseStatusException responseStatusException) {
            HttpStatus statusCode = (HttpStatus) responseStatusException.getStatusCode();
            code = statusCode.value();
            if (code == 404) {
                message = "请求的服务或路由不存在";
            } else if (code == 503) {
                message = "请求的服务不可用";
            } else {
                message = responseStatusException.getReason();
            }
        } else {
            // 其他异常
            message = ex.getMessage() != null ? ex.getMessage() : "系统繁忙，请稍后再试";
        }

        log.error("[网关全局异常] 请求路径: {}, 异常信息: {}", exchange.getRequest().getPath(), ex.getMessage());

        // 统一按 RetObj 格式返回
        Map<String, Object> result = new HashMap<>();
        result.put("code", code);
        result.put("message", message);
        result.put("data", null);

        return response.writeWith(Mono.fromSupplier(() -> {
            DataBufferFactory bufferFactory = response.bufferFactory();
            try {
                return bufferFactory.wrap(objectMapper.writeValueAsBytes(result));
            } catch (JsonProcessingException e) {
                log.error("JSON 序列化异常", e);
                return bufferFactory.wrap(new byte[0]);
            }
        }));
    }
}
