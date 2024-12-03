package com.xf.demo.service;

import com.xf.demo.mapper.SeataOrderMapper;
import io.seata.spring.annotation.GlobalTransactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * packageName com.xf.demo.service
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
