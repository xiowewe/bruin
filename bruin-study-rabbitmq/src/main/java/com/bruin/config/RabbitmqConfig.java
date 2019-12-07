package com.bruin.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * @description:
 * @author: xiongwenwen   2019/11/29 14:13
 */
@Configuration
public class RabbitmqConfig {
    public final static String DIRECT_QUEUE = "direct_queue";
    public final static String TOPIC_QUEUE = "topic_queue";
    public final static String FANOUT_QUEUE = "fanout_queue";
    public final static String FANOUT_QUEUE_ANOTHER = "fanout_queue_another";

    public final static String DIRECT_EXCHANGE = "direct_exchange";
    public final static String TOPIC_EXCHANGE = "topic_exchange";
    public final static String FANOUT_EXCHANGE = "fanout_exchange";

    public final static String TOPIC_ROUTINGKEY = "_key";

    public static final String DELAY_EXCHANGE_NAME = "delay_exchange";

    @Bean
    public Queue directQueue(){
        return new Queue(DIRECT_QUEUE, true);
    }

    @Bean
    public Queue topicQueue(){
        return new Queue(TOPIC_QUEUE, true);
    }

    @Bean
    public Queue fanoutQueue(){
        return new Queue(FANOUT_QUEUE, true);
    }

    @Bean
    public Queue fanoutQueueAnother(){
        return new Queue(FANOUT_QUEUE_ANOTHER, true);
    }


    @Bean
    public DirectExchange directExchange(){
        return new DirectExchange(DIRECT_EXCHANGE);
    }

    @Bean
    public TopicExchange topicExchange(){
        return new TopicExchange(TOPIC_EXCHANGE);
    }

    @Bean
    public FanoutExchange fanoutExchange(){
        return new FanoutExchange(FANOUT_EXCHANGE);
    }



    @Bean
    public Binding topicExchangeBinding(){
        return BindingBuilder.bind(topicQueue()).to(topicExchange()).with(TOPIC_ROUTINGKEY);
    }

    @Bean
    public Binding fanoutExchangeBinding() {
        return BindingBuilder.bind(fanoutQueue()).to(fanoutExchange());
    }

    @Bean
    public Binding fanoutExchangeAnotherBinding() {
        return BindingBuilder.bind(fanoutQueueAnother()).to(fanoutExchange());
    }

//    @Bean(DELAY_EXCHANGE_NAME)
//    public Exchange delayExchange() {
//        Map<String, Object> args = new HashMap<>();
//        args.put("x-delayed-type", "direct");
//        return new CustomExchange(DELAY_EXCHANGE_NAME, "x-delayed-message", true, false, args);
//    }
}
