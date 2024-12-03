package com.xf.demo.service;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * packageName com.xf.demo.controller.service
 * @author remaindertime
 * @className FeignService
 * @date 2024/11/29
 * @description 远程调用接口
 */
@FeignClient(name = "cloud-gateway")
public interface FeignService {

    @GetMapping(value = "/test/feign/{str}")
    String feignInfo(@PathVariable("str") String str);

    @GetMapping(value = "/test/feign01")
    String feignInfo01();

    @GetMapping(value = "/test/seata/deInventory")
    void deInventorySeata(@RequestParam("num") Integer num,@RequestParam("productId") Integer productId);

}
