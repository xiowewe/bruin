package com.bruin.expansion.spring.listener;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * @description: {@link DemoApplicationListener} 测试类
 * @author: xiongwenwen   2020/5/14 17:39
 */
@Configuration
@ComponentScan("com.bruin.expansion.spring.listener")
public class DemoListenerTest {
    public static void main(String[] args) {
        AnnotationConfigApplicationContext application = new AnnotationConfigApplicationContext(DemoListenerTest.class);

        DemoEventPublisher publisher = (DemoEventPublisher) application.getBean("demoEventPublisher");
        publisher.doEvent("test");
    }
}
