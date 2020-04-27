package com.bruin.expansion.spring;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/**
 * @description: spring schema 拓展类 xml parse
 * @author: xiongwenwen   2020/4/27 14:17
 */
public class UserBeanDefinitionParser implements BeanDefinitionParser {

    private final Class<?> beanClass;

    public UserBeanDefinitionParser(Class<?> beanClass) {
        this.beanClass = beanClass;
    }

    @Override
    public BeanDefinition parse(Element element, ParserContext parserContext) {
        return parse(element, parserContext, beanClass);
    }

    private BeanDefinition parse(Element element, ParserContext parserContext, Class<?> beanClass){
        RootBeanDefinition beanDefinition = new RootBeanDefinition();

        beanDefinition.setBeanClass(beanClass);
        beanDefinition.setLazyInit(false);
        String name = element.getAttribute("name");
        beanDefinition.getPropertyValues().addPropertyValue("name", name);
        parserContext.getRegistry().registerBeanDefinition(name, beanDefinition);

        return beanDefinition;
    }
}
