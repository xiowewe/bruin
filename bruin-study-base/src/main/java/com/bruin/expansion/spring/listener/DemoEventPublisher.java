package com.bruin.expansion.spring.listener;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.stereotype.Component;

/**
 * @description: DemoEvent 发布类，实现 {@link ApplicationEventPublisherAware}具有事件发布能力
 * @author: xiongwenwen   2020/5/14 17:08
 */
@Component
public class DemoEventPublisher implements ApplicationEventPublisherAware {

    private ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }


    public void doEvent(String name){
        System.out.println("我是" + name + "准备做...");
        //发布event
        applicationEventPublisher.publishEvent(new DemoApplicationEvent(name));
    }

}
