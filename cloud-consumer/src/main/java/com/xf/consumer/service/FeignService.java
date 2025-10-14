package com.xf.consumer.service;

import com.xf.consumer.config.FeignConfiguration;
import com.xf.consumer.service.fallback.FeignServiceFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * packageName com.xf.producer.controller.service
 * @author remaindertime
 * @className FeignService
 * @date 2024/11/29
 * @description 远程调用接口
 */
//普通使用
//@FeignClient(name = "cloud-gateway")
//集成sentinel 全部feign接口实现熔断降级
@FeignClient(name = "cloud-producer", fallback = FeignServiceFallback.class, configuration = FeignConfiguration.class)
public interface FeignService {

    @GetMapping(value = "/test/feign/{str}")
    String feignInfo(@PathVariable("str") String str);

    @GetMapping(value = "/test/feign01")
    String feignInfo01();

    @GetMapping(value = "/test/seata/deInventory")
    void deInventorySeata(@RequestParam("num") Integer num,@RequestParam("productId") Integer productId);

}
