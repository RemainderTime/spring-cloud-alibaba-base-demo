package com.xf.demo.controller;

import com.xf.demo.service.FeignService;
import com.xf.demo.service.SeataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * packageName com.xf.demo.controller
 * @author remaindertime
 * @className FeignController
 * @date 2024/11/29
 * @description
 */
@RestController
public class FeignController {

    @Autowired
    private FeignService feignService;

    @Autowired
    private SeataService seataService;

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
     *  使用分布式事务 seata
     * @return
     */
    @GetMapping(value = "/seata")
    public String seataOrder() {
        return seataService.placeOrderSeata();
    }
}
