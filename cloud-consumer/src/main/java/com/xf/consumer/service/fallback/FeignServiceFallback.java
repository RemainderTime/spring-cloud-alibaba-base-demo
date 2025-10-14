package com.xf.consumer.service.fallback;

import com.xf.consumer.service.FeignService;

/**
 * packageName com.xf.producer.service.fallback
 * @author remaindertime
 * @className FeignServiceFallback
 * @date 2024/12/4
 * @description feign 集成 sentinel 降级处理实现
 */
public class FeignServiceFallback implements FeignService {
    @Override
    public String feignInfo(String str) {
        return "feignInfo fallback";
    }

    @Override
    public String feignInfo01() {
        return "feignInfo01 fallback";
    }

    @Override
    public void deInventorySeata(Integer num, Integer productId) {
        // 不做额外的降级处理，继续抛出异常，使分布式事务seata有效
        throw new RuntimeException("Service unavailable");
    }
}
