package com.bruin;

import com.bruin.expansion.spring.namespase.ApplicationConfig;
import com.bruin.expansion.spring.namespase.ServiceBean;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ImportResource;

@SpringBootApplication
@ImportResource(locations = {"classpath:user.xml"})
public class BruinStudyBaseApplication {

    public static void main(String[] args) {
//        SpringApplication.run(BruinStudyBaseApplication.class, args);
        ConfigurableApplicationContext applicationContext = SpringApplication.run(BruinStudyBaseApplication.class, args);

        //spring schema 拓展demo
        ServiceBean serviceBean = applicationContext.getBean(ServiceBean.class);
        System.out.println(serviceBean.getName());

        ApplicationConfig applicationConfig = applicationContext.getBean(ApplicationConfig.class);
        System.out.println(applicationConfig.getName());
    }

}
