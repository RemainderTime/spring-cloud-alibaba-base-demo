package com.xf.demo.config;


import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.spring.autoconfigure.RocketMQAutoConfiguration;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * RocketMQConfig
 *
 * @author 海言
 * @date 2025/10/10
 * @time 15:15
 * @Description
 */
@Configuration
@Import({RocketMQAutoConfiguration.class})
public class RocketMQConfig {


    @Value("${spring.rocketmq.name-server}")
    private String nameServer;
    @Value("${spring.rocketmq.producer.group}")
    private String producerGroup;

    /**
     * 生产者 Bean，手动配置参数，生产环境可扩展
     */
    @Bean(destroyMethod = "shutdown")
    public DefaultMQProducer defaultMQProducer() throws MQClientException {
        DefaultMQProducer producer = new DefaultMQProducer(producerGroup);
        producer.setNamesrvAddr(nameServer);

        // 生产环境推荐设置
        producer.setSendMsgTimeout(5000);                // 消息发送超时
        producer.setRetryTimesWhenSendFailed(3);        // 发送失败重试次数
        producer.setMaxMessageSize(1024 * 1024 * 4);    // 最大消息体 4MB
        return producer;
    }

    /**
     * RocketMQTemplate 注入
     */
    @Bean
    public RocketMQTemplate rocketMQTemplate(DefaultMQProducer producer) {
        RocketMQTemplate template = new RocketMQTemplate();
        template.setProducer(producer);
        return template;
    }
}
