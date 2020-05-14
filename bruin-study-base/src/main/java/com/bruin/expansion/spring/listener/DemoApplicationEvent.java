package com.bruin.expansion.spring.listener;

import org.springframework.context.ApplicationEvent;

/**
 * @description: Demo事件类
 * @author: xiongwenwen   2020/5/14 17:05
 */
public class DemoApplicationEvent extends ApplicationEvent {

    private String name;

    public DemoApplicationEvent(Object source) {
        super(source);
    }

    public DemoApplicationEvent(Object source, String name) {
        super(source);
        this.name = name;
    }
}
