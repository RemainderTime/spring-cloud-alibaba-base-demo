package com.xf.demo.controller;

import com.xf.demo.controller.service.FeignService;
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

    /**
     * 使用openFeign远程调用
     * @param str
     * @return
     */
    @GetMapping(value = "/feign/{str}")
    public String rest(@PathVariable String str) {
        return feignService.feignInfo(str);
    }
}
