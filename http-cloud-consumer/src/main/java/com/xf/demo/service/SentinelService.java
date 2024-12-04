package com.xf.demo.service;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.xf.demo.utils.SentinelExceptionUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * packageName com.xf.demo.service
 * @author remaindertime
 * @className SentinelService
 * @date 2024/12/4
 * @description sentinel 相关验证类
 */
@Service
public class SentinelService {

    @Autowired
    private FeignService feignService;

    @SentinelResource(value = "sentinelOrFeign", fallback = "fallbackHandler", fallbackClass = SentinelExceptionUtil.class,
            blockHandler = "blockExHandler", blockHandlerClass = SentinelExceptionUtil.class)
    public String sentinelOrFeign(){

        return feignService.feignInfo01();
    }

    @SentinelResource(value = "sentinelLocal", fallback = "fallbackHandler", fallbackClass = SentinelExceptionUtil.class,
            blockHandler = "blockExHandler", blockHandlerClass = SentinelExceptionUtil.class)
    public String sentinelLocal(){

//        throw new RuntimeException("测试sentinel本地调用异常捕获");
        return "sentinel本地调用成功";
    }
}
