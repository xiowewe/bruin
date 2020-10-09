package com.bruin.spring.ioc.container;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;

/**
 * @description:
 * @author: xiongwenwen
 * @date: 2020/9/20 21:46
 */
@ComponentScan("com.bruin.spring.ioc.container")
public class FactoryBeanDemo {
    public static void main(String[] args) {
        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext();
        applicationContext.register(FactoryBeanDemo.class);
        applicationContext.refresh();

        Object customerFactoryBean =  applicationContext.getBean("&customerFactoryBean");

        Object customerService = applicationContext.getBean(CustomerService.class);

        System.out.println(customerFactoryBean);
        System.out.println(customerService);

        System.out.println(customerFactoryBean == customerService);
    }
}
