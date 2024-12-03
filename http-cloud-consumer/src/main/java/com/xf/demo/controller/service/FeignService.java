package com.xf.demo.controller.service;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

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
}
