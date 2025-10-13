package com.xf.demo.mq;

import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.List;

/**
 * packageName com.xf.demo.mq
 * @author remaindertime
 * @className MessageProducer
 * @date 2024/12/6
 * @description 消息生产者
 */
@Service
public class RocketMQMessageProducer {

    // 自动注入配置文件中绑定的通道

    @Resource
    private RocketMQTemplate rocketMQTemplate;

    /**
     * 发送普通消息
     */
    public void sendMessage(String topic, String msg) {
        rocketMQTemplate.convertAndSend(topic, msg);
    }

    /**
     * 发送带Tag的消息
     */
    public void sendMessage(String topic, String tag, String msg) {
        rocketMQTemplate.convertAndSend(topic + ":" + tag, msg);
    }

    /**
     * 发送顺序消息（保证同一队列顺序）
     */
    public void sendOrderlyMessage(String topic, String msg, String hashKey) {
        rocketMQTemplate.syncSendOrderly(topic, msg, hashKey);
    }

    /**
     * 发送事务消息
     */
    public void sendTransactionMessage(String topic, String msg, Object arg) {
        rocketMQTemplate.sendMessageInTransaction(topic, MessageBuilder.withPayload(msg).build(), arg);
    }

    /**
     * 批量发送消息
     */
    public void sendBatchMessage(String topic, List<String> messages) {
        messages.forEach(msg -> rocketMQTemplate.convertAndSend(topic, msg));
    }

}