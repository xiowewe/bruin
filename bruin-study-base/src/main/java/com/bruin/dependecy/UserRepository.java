package com.bruin.dependecy;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.stereotype.Component;

@Component
public class UserRepository {

    private BeanFactory beanFactory;

    public BeanFactory getBeanFactory() {
        return beanFactory;
    }

    public void setBeanFactory(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }
}
