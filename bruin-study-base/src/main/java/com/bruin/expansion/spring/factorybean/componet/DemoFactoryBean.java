package com.bruin.expansion.spring.factorybean.componet;

import com.bruin.expansion.spring.factorybean.service.TestService;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.stereotype.Component;

@Component
public class DemoFactoryBean implements FactoryBean {

    @Override
    public Object getObject() throws Exception {
        return new TestService();
    }

    @Override
    public Class<?> getObjectType() {
        return TestService.class;
    }
}
