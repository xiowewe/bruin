package com.bruin.controller;

import com.bruin.config.RabbitmqConfig;
import com.bruin.provider.RabbitmqProvider;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @description:
 * @author: xiongwenwen   2019/11/29 18:34
 */
@RestController
@RequestMapping("/mq")
public class TestController {

    @RequestMapping("/sendMsg")
    public void sendMsg(){
        RabbitmqProvider.sendMsg(RabbitmqConfig.DIRECT_QUEUE, "sendMsg");
    }

    @RequestMapping("/sendDirectMsg")
    public void sendDirectMsg(){
        RabbitmqProvider.sendDirectMsg(RabbitmqConfig.DIRECT_EXCHANGE, RabbitmqConfig.DIRECT_QUEUE, RabbitmqConfig.DIRECT_QUEUE, "sendDirectMsg");
    }
}
