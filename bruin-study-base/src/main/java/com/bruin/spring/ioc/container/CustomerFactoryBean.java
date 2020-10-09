package com.bruin.spring.ioc.container;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.stereotype.Component;

/**
 * @description:
 * @author: xiongwenwen
 * @date: 2020/9/20 21:44
 */
@Component
public class CustomerFactoryBean implements FactoryBean {
    @Override
    public Object getObject() throws Exception {
        return new CustomerService();
    }

    @Override
    public Class<?> getObjectType() {
        return CustomerService.class;
    }
}
