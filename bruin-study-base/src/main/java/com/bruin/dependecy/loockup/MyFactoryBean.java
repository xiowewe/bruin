package com.bruin.dependecy.loockup;

import org.springframework.beans.factory.FactoryBean;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class MyFactoryBean implements FactoryBean {

    private Class<?> interfaceClass;
    private Object target;

    private Object object;

    public MyFactoryBean(Class<?> interfaceClass, Object target) {
        this.interfaceClass = interfaceClass;
        this.target = target;

        this.object = Proxy.newProxyInstance(
                this.getClass().getClassLoader(),
                new Class[]{interfaceClass},
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        System.out.println("invoke method:" + method.getName());
                        System.out.println("invoke method before" + System.currentTimeMillis());

                        Object object = method.invoke(method, args);

                        System.out.println("invoke method after" + System.currentTimeMillis());

                        return object;
                    }
                }
        );
    }

    @Override
    public Object getObject() throws Exception {
        return object;
    }

    @Override
    public Class<?> getObjectType() {
        return object == null ? Object.class : object.getClass();
    }
}
