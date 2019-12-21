package com.bruin.config;


import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * @description:
 * @author: xiongwenwen   2019/12/20 14:56
 */
@Component
public class KafkaComsumer {

    @KafkaListener(topics = "test_topic", groupId = "test_group")
    public void receive(@Payload(required = false) String message, @Headers MessageHeaders headers){
        System.out.println(message);
    }

    @KafkaListener(topics = "test_topic1", groupId = "test_group")
    public void receive1(@Payload(required = false) String message, @Headers MessageHeaders headers){
        System.out.println(message);
    }

}
