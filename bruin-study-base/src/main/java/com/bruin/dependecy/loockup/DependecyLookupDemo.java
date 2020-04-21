package com.bruin.dependecy.loockup;

import com.bruin.dependecy.UserRepository;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component
public class DependecyLookupDemo implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    public void test(){

        System.out.println("内建：" + applicationContext.getBean(ApplicationContext.class));
        System.out.println(applicationContext.getBean(UserRepository.class).getBeanFactory());
    }

}

