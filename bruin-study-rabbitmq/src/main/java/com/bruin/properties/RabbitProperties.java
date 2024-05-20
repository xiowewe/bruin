package com.bruin.properties;

import lombok.Data;

/**
 * @author xiongwenwen
 * @since 2023/9/14 11:27 AM
 */
@Data
public class RabbitProperties {

    public String DIRECT_QUEUE = "direct_queue";
    public String TOPIC_QUEUE = "topic_queue";
    public String FANOUT_QUEUE = "fanout_queue";
    public String FANOUT_QUEUE_ANOTHER = "fanout_queue_another";

    public String DIRECT_EXCHANGE = "direct_exchange";
    public String TOPIC_EXCHANGE = "topic_exchange";
    public String FANOUT_EXCHANGE = "fanout_exchange";
    public String DELAY_EXCHANGE = "delay_exchange";
    
    public String TOPIC_ROUTING_KEY = "_key";
}
