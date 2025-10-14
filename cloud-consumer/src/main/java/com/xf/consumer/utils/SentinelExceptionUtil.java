package com.xf.consumer.utils;

import com.alibaba.csp.sentinel.slots.block.BlockException;

/**
 * packageName com.xf.consumer.dubbo.utils
 * @author remaindertime
 * @className SentinelExectionUtil
 * @date 2024/11/25
 * @description sentinel 异常处理类
 */
public class SentinelExceptionUtil {

    /**
     * Sentinel 规则的 流控、熔断、热点参数限流 等被触发时调用。优先级高于 fallback
     * @param ex
     * @return
     */
    public static String blockExHandler(BlockException ex) {
        ex.printStackTrace();
        return "Sentinel 规则的 流控、熔断、热点参数限流 等被触发:" + ex.getMessage();
    }

    /**
     * 业务异常（如运行时异常）或其他未被 Sentinel 规则拦截的异常触发。
     * @param ex
     * @return
     */
    public static String fallbackHandler(Throwable ex) {
        ex.printStackTrace();
        return "业务异常或其他未被 Sentinel 规则拦截的异常触发: " + ex.getMessage();
    }
}
