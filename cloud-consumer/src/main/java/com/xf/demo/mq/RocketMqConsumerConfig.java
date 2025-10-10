package com.xf.demo.mq;


import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.message.MessageExt;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.charset.StandardCharsets;

/**
 * RocketMqConsumerConfig
 *
 * @author 海言
 * @date 2025/10/10
 * @time 16:48
 * @Description
 */
@Slf4j
@Configuration
public class RocketMqConsumerConfig {

    @Value("${spring.rocketmq.name-server}")
    private String nameServer;

    @Value("${spring.rocketmq.consumer.group}")
    private String consumerGroup;

    @Bean(initMethod = "start", destroyMethod = "shutdown")
    public DefaultMQPushConsumer multiTopicConsumer() throws MQClientException {
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer(consumerGroup);
        consumer.setNamesrvAddr(nameServer);

        // 消费线程
        consumer.setConsumeThreadMin(5);
        consumer.setConsumeThreadMax(20);
        // 消费失败重试
        consumer.setMaxReconsumeTimes(3);
        // 多 Topic 订阅
        consumer.subscribe("user-topic", "*");
        consumer.subscribe("order-topic", "pay||refund");
        // 消息监听器
        consumer.registerMessageListener((MessageListenerConcurrently) (msgs, ctx) -> {
            for (MessageExt msg : msgs) {
                String topic = msg.getTopic();
                String tag = msg.getTags();
                String body = new String(msg.getBody(), StandardCharsets.UTF_8);

                switch (topic) {
                    case "user-topic" -> handleUserTopic(tag, body);
                    case "order-topic" -> handleOrderTopic(tag, body);
                    default -> log.warn("未知 topic: {}", topic);
                }
            }
            return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
        });

        return consumer;
    }

    private void handleUserTopic(String tag, String body) {
        log.info("处理 user-topic, tag={}, body={}", tag, body);
    }

    private void handleOrderTopic(String tag, String body) {
        log.info("处理 order-topic, tag={}, body={}", tag, body);
    }
}
