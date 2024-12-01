package com.xf.demo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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

    @GetMapping(value = "/test/{string}")
    public String test(@PathVariable String string) {
        return string;
    }
}
