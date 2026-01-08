package com.xf.cloudcommon.config;

import com.xf.cloudcommon.utils.UserContextHolder;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import com.alibaba.fastjson2.JSON;

/**
 * @Description: Feign 调用拦截器，透传用户信息和 Internal-Secret
 */
@Slf4j
@Configuration
public class FeignConfig implements RequestInterceptor {

    private static final String SECRET_KEY = "expected-secret";

    @Override
    public void apply(RequestTemplate template) {
        // 1. 设置内部调用密钥，防止绕过网关
        template.header("X-Internal-Auth", SECRET_KEY);

        // 2. 传递用户信息 (如果有)
        Map<String, String> userInfo = UserContextHolder.get();
        if (userInfo != null && !userInfo.isEmpty()) {
             try {
                String json = JSON.toJSONString(userInfo);
                String base64 = Base64.getEncoder().encodeToString(json.getBytes(StandardCharsets.UTF_8));
                template.header("X-UserInfo", base64);
            } catch (Exception e) {
                log.error("Feign用户信息编码失败", e);
            }
        }
    }
}
