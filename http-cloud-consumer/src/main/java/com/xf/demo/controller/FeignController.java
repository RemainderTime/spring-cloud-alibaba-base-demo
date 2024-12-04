package com.xf.demo.controller;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.xf.demo.service.FeignService;
import com.xf.demo.service.SeataService;
import com.xf.demo.service.SentinelService;
import com.xf.demo.utils.SentinelExceptionUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * packageName com.xf.demo.controller
 * @author remaindertime
 * @className FeignController
 * @date 2024/11/29
 * @description 控制器
 */
@RestController
public class FeignController {

    @Autowired
    private FeignService feignService;

    @Autowired
    private SeataService seataService;

    @Autowired
    private SentinelService sentinelService;

    /**
     * 使用openFeign远程调用
     * @param str
     * @return
     */
    @GetMapping(value = "/feign/{str}")
    public String rest(@PathVariable String str) {
        return feignService.feignInfo(str);
    }

    /**
     * 使用openFeign远程调用  无参数
     * @return
     */
    @GetMapping(value = "/feign01")
    public String rest01() {
        return feignService.feignInfo01();
    }

    /**
     *  使用分布式事务 seata 远程调用Feign 以及 限流熔断sentinel注解
     * @return
     */
    @GetMapping(value = "/seataOrFeignOrSentinel")
    @SentinelResource(value = "seataOrFeignOrSentinel", fallback = "fallbackHandler", fallbackClass = SentinelExceptionUtil.class)
    public String seataOrder() {
        return seataService.placeOrderSeata();
    }

    /**
     *   使用sentinel 限流熔断注解并集合使用feign远程调用
     * @return
     */
    @GetMapping(value = "/sentinelOrFeign")
    public String sentinelOrFeign() {
        return sentinelService.sentinelOrFeign();
    }

    /**
     *   使用sentinel 限流熔断注解  本地调用
     * @return
     */
    @GetMapping(value = "/sentinelLocal")
    public String sentinelLocal() {
        return sentinelService.sentinelLocal();
    }
}
