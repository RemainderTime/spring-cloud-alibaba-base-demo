package com.xf.cloudcommon.filter;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.xf.cloudcommon.utils.UserContextHolder;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

/**
 * RequestHeaderFilter
 *
 * @author 海言
 * @date 2025/9/5
 * @time 15:29
 * @Description 请求头过滤器
 */
@Configuration
public class RequestHeaderFilter implements Filter {

    private static final String INTERNAL_SECRET = "expected-secret";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        String header = req.getHeader("X-Internal-Auth");
        if (!INTERNAL_SECRET.equals(header)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "非法访问接口，禁止绕过网关访问");
        }
        String userBase64 = req.getHeader("X-UserInfo"); // 返回 String
        if(!StringUtils.isEmpty(userBase64)){
            //用户信息转码
            String userJson = new String(Base64.getDecoder().decode(userBase64), StandardCharsets.UTF_8);
            Map<String, String> map = JSON.parseObject(userJson, new TypeReference<>() {});
            //将用户信息设置到自定义context中
            UserContextHolder.set(map);
        }
        chain.doFilter(request, response);
    }
}
