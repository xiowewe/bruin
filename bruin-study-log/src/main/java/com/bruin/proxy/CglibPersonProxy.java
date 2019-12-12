package com.bruin.proxy;


import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;

/**
 * @description:
 * @author: xiongwenwen   2019/12/11 16:21
 */
public class CglibPersonProxy implements MethodInterceptor {
    private Object delegate;

    @Override
    public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
        System.out.println("proxy before...");

        Object result = methodProxy.invokeSuper(method, objects);

        System.out.println("proxy before...");


        return result;
    }

    public static Person getProxyInstance(){
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(Person.class);

        enhancer.setCallback(new CglibPersonProxy());
        return (Person) enhancer.create();
    }
}
