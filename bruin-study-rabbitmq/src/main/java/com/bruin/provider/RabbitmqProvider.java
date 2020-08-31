package com.bruin.provider;

import com.alibaba.fastjson.JSON;
import com.bruin.config.FastJsonMessageConverter;
import com.bruin.config.RabbitmqConfig;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.util.UUID;

import static org.springframework.amqp.support.converter.SerializerMessageConverter.DEFAULT_CHARSET;

/**
 * @description:
 * @author: xiongwenwen   2019/11/29 14:48
 */
@Component
public class RabbitmqProvider implements ApplicationContextAware {

    public static final Long MAX_VALUE = 0xffffffffL;

    private ApplicationContext applicationContext;
    private static RabbitTemplate rabbitTemplate;
    private static RabbitAdmin rabbitAdmin;
    private static Exchange delayExchange;

//    @Autowired
//    private FastJsonMessageConverter fastJsonMessageConverter;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @PostConstruct
    private void init(){
        rabbitTemplate = applicationContext.getBean(RabbitTemplate.class);
//        rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter());

        rabbitAdmin = new RabbitAdmin(rabbitTemplate);
//        delayExchange = (Exchange) applicationContext.getBean(RabbitmqConfig.DELAY_EXCHANGE_NAME);
    }


    /**
     *  exchange、routeKey未默认的 ""
     * @param queueName
     * @param obj
     */
    public static void sendMsg(String queueName, Object obj){
        CorrelationData correlationId = new CorrelationData(UUID.randomUUID().toString());
        rabbitAdmin.declareQueue(new Queue(queueName));

        rabbitTemplate.convertAndSend(queueName, obj, correlationId);
    }

    /**
     * 指定exchange、routeKey，不存在则declare
     * @param exchange
     * @param routeKey
     * @param queueName
     * @param obj
     */
    public static void sendDirectMsg(String exchange, String routeKey, String queueName, Object obj){
        CorrelationData correlationData = new CorrelationData(UUID.randomUUID().toString());
        rabbitTemplate.setExchange(exchange);
        rabbitTemplate.setRoutingKey(routeKey);

        rabbitAdmin.declareQueue(new Queue(queueName));
        DirectExchange directExchange = new DirectExchange(exchange);
        rabbitAdmin.declareExchange(directExchange);
        Binding binding = BindingBuilder.bind(new Queue(queueName)).to(directExchange).with(routeKey);
        rabbitAdmin.declareBinding(binding);

        rabbitTemplate.convertAndSend(exchange, routeKey , obj, correlationData);
    }

    /**
     * 不指定queue
     * @param exchange
     * @param obj
     */
    public static void sendFanoutMsg(String exchange, String routKey, Object obj){
        CorrelationData correlationData = new CorrelationData(UUID.randomUUID().toString());
        rabbitAdmin.declareExchange(new FanoutExchange(exchange));

        rabbitTemplate.convertAndSend(exchange , routKey, obj, correlationData);
    }

    /**
     * 指定queue，不存在则declare
     * @param exchange
     * @param routKey
     * @param queueName
     * @param obj
     */
    public static void sendFanoutMsg(String exchange, String routKey, String queueName, Object obj){
        CorrelationData correlationData = new CorrelationData(UUID.randomUUID().toString());
        rabbitTemplate.setExchange(exchange);

        rabbitAdmin.declareQueue(new Queue(queueName));
        FanoutExchange fanoutExchange = new FanoutExchange(exchange);
        rabbitAdmin.declareExchange(fanoutExchange);
        Binding binding = BindingBuilder.bind(new Queue(queueName)).to(fanoutExchange);
        rabbitAdmin.declareBinding(binding);

        rabbitTemplate.convertAndSend(exchange , routKey, obj, correlationData);
    }


    public static void sendDelay(String queueName, String routKey, Object data, long delayMilliSeconds) throws IllegalArgumentException {
        if(delayMilliSeconds > MAX_VALUE) {
            throw new IllegalArgumentException("超时过长, 只支持 < 4294967296 的延时值");
        }
        Binding binding = BindingBuilder.bind(new Queue(queueName)).to(delayExchange).with(routKey).noargs();
        rabbitAdmin.declareBinding(binding);

        CorrelationData correlationId = new CorrelationData(UUID.randomUUID().toString());
        rabbitTemplate.convertAndSend(RabbitmqConfig.DELAY_EXCHANGE_NAME, queueName, data, message -> {
            MessageProperties messageProperties = message.getMessageProperties();
            messageProperties.getHeaders().put("x-delay", delayMilliSeconds);
            return message;
        }, correlationId);
    }
}
