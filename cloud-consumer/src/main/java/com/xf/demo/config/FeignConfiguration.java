package com.xf.demo.config;

import com.xf.demo.service.fallback.FeignServiceFallback;
import org.springframework.context.annotation.Bean;

/**
 * packageName com.xf.demo.config
 * @author remaindertime
 * @className FeignConfiguration
 * @date 2024/12/4
 * @description sentinel降级处理配置
 */
public class FeignConfiguration {
    @Bean
    public FeignServiceFallback echoServiceFallback() {
        return new FeignServiceFallback();
    }
}