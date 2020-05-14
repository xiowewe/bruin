package com.bruin.expansion.spring.listener;

import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

/**
 * @description: listener demo类
 * @author: xiongwenwen   2020/5/14 17:12
 */
@Component
public class DemoApplicationListener implements ApplicationListener<DemoApplicationEvent> {

    @Override
    public void onApplicationEvent(DemoApplicationEvent event) {
        System.out.println("Demo Event开始执行");
    }
}
