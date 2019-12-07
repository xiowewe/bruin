package com.bruin.consumer;

import com.alibaba.fastjson.JSON;
import com.bruin.config.RabbitmqConfig;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @description:
 * @author: xiongwenwen   2019/11/29 15:10
 */
@Component
public class RabbitmqConsumer {
    private static final Logger logger = LoggerFactory.getLogger(RabbitmqConsumer.class);
    public static final String DEFAULT_CHARSET = "UTF-8";

    /**
     * @Bean 申明queue
     */
    @RabbitListener(queues = RabbitmqConfig.DIRECT_QUEUE)
    public void directMsg(@Payload String msg, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws Exception {

        logger.info("接受消息：{}", msg);

        try {
            channel.basicAck(tag, false);

            logger.info("----------关闭通道--------");
        } catch (IOException e) {
            logger.error("异常", e);
        }
    }

    /**
     * queuesToDeclare 申明queue
     * @param message
     */
    @RabbitListener(queuesToDeclare = @Queue("test_queue"))
    public void testQueueConsumer(Message message) throws Exception {
        String msg = new String(message.getBody(), DEFAULT_CHARSET);
        logger.info("接受消息：{}", msg);
    }


    @RabbitListener(queues = RabbitmqConfig.FANOUT_QUEUE)
    public void fanoutQueue(Message message) throws Exception {
        String msg = new String(message.getBody(), DEFAULT_CHARSET);

        logger.info("fanout_queue接受消息：{}", msg);
    }

    @RabbitListener(queues = RabbitmqConfig.FANOUT_QUEUE_ANOTHER)
    public void fanoutAnotherQueue(Message message) throws Exception {
        String msg = new String(message.getBody(), DEFAULT_CHARSET);

        logger.info("fanout_queue_another接受消息：{}", msg);
    }
}
