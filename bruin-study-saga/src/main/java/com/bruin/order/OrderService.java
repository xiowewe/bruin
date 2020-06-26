package com.bruin.order;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class OrderService {

    private RabbitTemplate rabbitTemplate;
    @Resource
    private OrderRepository orderRepository;

    public Order createOrder(OrderDetails details){

        return null;
    }
}
