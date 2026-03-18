package com.xf.cloudcommon.config;

import org.slf4j.MDC;
import org.springframework.core.task.TaskDecorator;

import java.util.Map;

/**
 * MDC 装饰器
 * 用于在 Spring 线程池中传递 MDC 上下文 (traceId)
 */
public class MdcTaskDecorator implements TaskDecorator {

    @Override
    public Runnable decorate(Runnable runnable) {
        // 1. 获取当前主线程的 MDC 内容
        Map<String, String> contextMap = MDC.getCopyOfContextMap();

        return () -> {
            try {
                // 2. 将内容设置进子线程（线程池线程）
                if (contextMap != null) {
                    MDC.setContextMap(contextMap);
                }
                runnable.run();
            } finally {
                // 3. 必须清理，防止线程复用带来的日志污染
                MDC.clear();
            }
        };
    }
}
