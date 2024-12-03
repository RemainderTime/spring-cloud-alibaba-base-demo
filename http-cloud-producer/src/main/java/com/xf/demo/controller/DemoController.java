package com.xf.demo.controller;

import com.xf.demo.mapper.SeataProductMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * packageName com.xf.demo.controller
 * @author remaindertime
 * @className DemoController
 * @date 2024/11/28
 * @description
 */
@RestController
public class DemoController {

    @Autowired
    private SeataProductMapper seataProductMapper;

    @GetMapping(value = "/test/feign/{string}")
    public String test(@PathVariable String string) {
        return string;
    }

    @GetMapping(value = "/test/feign01")
    public String test01() {
        return "成功了~~~";
    }

    @GetMapping(value = "/test/seata/deInventory")
    void deInventorySeata(@RequestParam("num") Integer num, @RequestParam("productId") Integer productId) {
        seataProductMapper.deInventory(num, productId);
    }
}
