package com.bruin.expansion.spring.factorybean.componet;

import com.bruin.expansion.spring.factorybean.service.TestService;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan("com.bruin.expansion.spring.factorybean.componet")
public class TestFactoryBean {
    public static void main(String[] args) {
        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext(TestFactoryBean.class);

        //返回注入
        System.out.println(applicationContext.getBean(TestService.class));

        System.out.println(applicationContext.getBean("demoFactoryBean"));

        System.out.println(applicationContext.getBean("&demoFactoryBean"));

    }
}
