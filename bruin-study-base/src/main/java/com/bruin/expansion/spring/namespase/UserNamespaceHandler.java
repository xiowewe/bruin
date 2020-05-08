package com.bruin.expansion.spring.namespase;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

/**
 * @description:spring schema 拓展类
 * @author: xiongwenwen   2020/4/27 14:15
 */
public class UserNamespaceHandler extends NamespaceHandlerSupport {

    @Override
    public void init() {
        super.registerBeanDefinitionParser("application", new UserBeanDefinitionParser(ApplicationConfig.class));
        super.registerBeanDefinitionParser("service", new UserBeanDefinitionParser(ServiceBean.class));
    }
}
