package com.xf.consumer.service;

import com.xf.consumer.mapper.SeataOrderMapper;
import io.seata.spring.annotation.GlobalTransactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * packageName com.xf.producer.service
 * @author remaindertime
 * @className SeataService
 * @date 2024/12/3
 * @description
 */
@Service
public class SeataService {

    @Autowired
    private FeignService feignService;
    @Autowired
    private SeataOrderMapper seataOrderMapper;

    // 注意：使用了分布式事务的方法不要使用sentinel 限流熔断注解@SentinelResource，否则会导致分布式事务失效
    @GlobalTransactional(name = "seata-order", rollbackFor = Exception.class)
    public String placeOrderSeata() {
        // 调用扣减库存的方法
        feignService.deInventorySeata(1, 1);
        // 调用创建订单的方法
        seataOrderMapper.createOrder(1, 1);
//        throw new RuntimeException("测试回滚");
        return "下单成功";
    }


}
