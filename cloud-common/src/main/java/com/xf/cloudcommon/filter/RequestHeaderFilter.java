package com.xf.cloudcommon.filter;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.xf.cloudcommon.constant.CommonConstant;
import com.xf.cloudcommon.utils.UserContextHolder;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
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
@Slf4j
public class RequestHeaderFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;

        // 1. 提取基础信息
        String uri = req.getRequestURI();
        String method = req.getMethod();
        // 获取 URL 后的参数 (?id=1)
        String queryString = req.getQueryString();
        // 1. 【2026-03-14新增】处理 TraceId (新增逻辑)
        String traceId = req.getHeader(CommonConstant.TRACE_ID_HEADER);
        if (StringUtils.hasText(traceId)) {
            // 将网关传来的 ID 存入 MDC，这样本服务后续的所有 log.info 都会带上它
            MDC.put(CommonConstant.TRACE_ID_HEADER, traceId);
        }
        log.info("[请求信息：] Method: {}, URI: {}, Params: {}", method, uri,
                StringUtils.hasText(queryString) ? queryString : "EMPTY");
        //2. 处理用户信息
        String header = req.getHeader(CommonConstant.X_INTERNAL_AUTH);
        if (!CommonConstant.SECRET_KEY.equals(header)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "非法访问接口，禁止绕过网关访问");
        }
        String userBase64 = req.getHeader("X-UserInfo");
        if(!StringUtils.isEmpty(userBase64)){
            //用户信息转码
            String userJson = new String(Base64.getDecoder().decode(userBase64), StandardCharsets.UTF_8);
            Map<String, String> map = JSON.parseObject(userJson, new TypeReference<>() {});
            //将用户信息设置到自定义context中
            UserContextHolder.set(map);
        }
        try {
            chain.doFilter(request, response);
        } finally {
            // 防止内存泄漏，必须清除 ThreadLocal
            UserContextHolder.clear();
            //【2026-03-14新增】清除 MDC 防止线程复用日志混乱
            MDC.remove(CommonConstant.TRACE_ID_HEADER);
        }
    }
}
