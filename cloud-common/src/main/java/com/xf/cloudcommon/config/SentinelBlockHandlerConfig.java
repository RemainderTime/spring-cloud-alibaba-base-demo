package com.xf.cloudcommon.config;

import com.alibaba.csp.sentinel.adapter.spring.webmvc.callback.BlockExceptionHandler;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.authority.AuthorityException;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeException;
import com.alibaba.csp.sentinel.slots.block.flow.FlowException;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowException;
import com.alibaba.csp.sentinel.slots.system.SystemBlockException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xf.cloudcommon.model.RetObj;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.io.PrintWriter;

/**
 * @Description: Sentinel 统一限流降级异常格式化处理器
 * @ClassName: SentinelBlockHandlerConfig
 */
@Slf4j
@Configuration
public class SentinelBlockHandlerConfig implements BlockExceptionHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, BlockException e) throws Exception {
        log.warn("Sentinel 触发拦截，请求 URI: {}, 拦截原因: {}", request.getRequestURI(), e.getClass().getSimpleName());

        String message = "系统繁忙，请稍后再试";

        // 判断具体是哪种 Sentinel 规则触发了拦截
        if (e instanceof FlowException) {
            message = "请求过于频繁，接口已被限流";
        } else if (e instanceof DegradeException) {
            message = "下游服务不稳定，触发熔断降级";
        } else if (e instanceof ParamFlowException) {
            message = "热点参数访问过于频繁，已被限流";
        } else if (e instanceof SystemBlockException) {
            message = "系统负载过高，触发系统保护机制";
        } else if (e instanceof AuthorityException) {
            message = "无权访问该资源，触发授权拦截";
        }

        // 设置 HTTP 状态码为 429 Too Many Requests
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        // 组装统一的 RetObj 返回格式
        RetObj<Object> retObj = RetObj.error(message);
        retObj.setCode(HttpStatus.TOO_MANY_REQUESTS.value()); // 让 code 与 HTTP 状态码保持一致，方便前端拦截器识别

        // 输出 JSON
        PrintWriter out = response.getWriter();
        out.print(objectMapper.writeValueAsString(retObj));
        out.flush();
        out.close();
    }
}
