package com.xf.gateway.filter;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xf.gateway.model.LoginUser;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.security.auth.login.LoginException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Map;

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
        String value = (String) redisTemplate.opsForValue().get("alibaba-token:" + jwt);
        if (StringUtils.isEmpty(value)) {
            throw new RuntimeException("请先登录");
        }
        JSONObject jsonObject = JSONObject.parseObject(value);
        //JSON对象转换成Java对象
        LoginUser loginUserInfo = JSONObject.toJavaObject(jsonObject, LoginUser.class);
        if (loginUserInfo == null || loginUserInfo.getId() <= 0) {
            throw new RuntimeException("用户登录异常");
        }
        // 修改请求头
        ServerHttpRequestDecorator decorator = new ServerHttpRequestDecorator(exchange.getRequest()) {
            @Override
            public HttpHeaders getHeaders() {
                HttpHeaders headers = new HttpHeaders();
                headers.putAll(super.getHeaders());  // 复制原始 headers
                headers.set("X-Internal-Auth", SECRET_KEY); // 安全加 header
                ObjectMapper mapper = new ObjectMapper();

                String userJson = JSON.toJSONString(mapper.convertValue(loginUserInfo, Map.class));
                //Header 默认只支持 ISO-8859-1,直接放中文 JSON 会被错误解码,所以传输时加密转码UTF-8
                String base64 = Base64.getEncoder().encodeToString(userJson.getBytes(StandardCharsets.UTF_8));
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
