package com.xf.demo.mq;

import org.apache.rocketmq.common.message.MessageConst;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * packageName com.xf.demo.mq
 * @author remaindertime
 * @className MessageProducer
 * @date 2024/12/6
 * @description 消息生产者
 */
@Service
public class MessageProducer {

    // 自动注入配置文件中绑定的通道

    @Autowired
    private StreamBridge streamBridge;

    // 消息通道
    public void sendMessageToOutput0(String messageContent) {
        Map<String, Object> headers = new HashMap<>();
        headers.put(MessageConst.PROPERTY_TAGS, "tag0");
        Message<String> msg = new GenericMessage<>(messageContent, headers);
        streamBridge.send("producer-out-0", msg);
    }

    public void sendMessageToOutput1(String messageContent) {
        Map<String, Object> headers = new HashMap<>();
        headers.put(MessageConst.PROPERTY_TAGS, "tag1");
        Message<String> msg = new GenericMessage<>(messageContent, headers);
        streamBridge.send("producer-out-1", msg);
    }

}