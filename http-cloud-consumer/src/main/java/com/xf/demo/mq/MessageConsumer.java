package com.xf.demo.mq;

import org.springframework.context.annotation.Bean;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;
import java.util.function.Consumer;

/**
 * packageName com.xf.demo.mq
 * @author remaindertime
 * @className MessageConsumer
 * @date 2024/12/6
 * @description 消费者
 */
@Component
public class MessageConsumer {

    /**
     *  通过方法名称自动绑定到符合条件的消费者通道
     *  （注：当前遇到的难点是一个消费者服务只能实现一个消费方法，实现多个消费方法会使消费功能失效）
     * @return
     */
    @Bean
    public Consumer<Message<String>> consumer1() {
        return msg -> {
            System.out.println(Thread.currentThread().getName() + " Consumer1 Receive New Messages: " + msg);
        };
    }
}
