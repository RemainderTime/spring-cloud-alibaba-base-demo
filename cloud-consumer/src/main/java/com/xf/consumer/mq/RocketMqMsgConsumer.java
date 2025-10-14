package com.xf.consumer.mq;


import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

/**
 * RocketMqMsgConsumer
 *
 * @author 海言
 * @date 2025/10/13
 * @time 15:29
 * @Description
 */
@Slf4j
@Component
@RocketMQMessageListener(topic = "user-topic", consumerGroup = "consumer-group")
public class RocketMqMsgConsumer implements RocketMQListener<String> {
    @Override
    public void onMessage(String message) {
        log.info("接收到消息----------：{}", message);
    }
}
