package com.bruin.dependecy.loockup;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

/**
 * @description:
 * @author: xiongwenwen
 * @date: 2020/9/27 21:03
 */
@ComponentScan("com.bruin.dependecy.loockup")
public class AnnotationDependencyLookup {
    public static void main(String[] args) {
        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext();
        applicationContext.register(AnnotationDependencyLookup.class);
        applicationContext.refresh();

        applicationContext.getBean("people");

        applicationContext.close();
    }
}
