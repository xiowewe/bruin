package com.bruin.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * @description:
 * @author: xiongwenwen   2019/12/11 16:07
 */
public class DynamicPersonProxy implements InvocationHandler {
    private Object delegate;


    public Object bind(Object delegate){
        this.delegate = delegate;
        return Proxy.newProxyInstance(delegate.getClass().getClassLoader(), delegate.getClass().getInterfaces(), this);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Object result = null;

        System.out.println("proxy before...");

        result = method.invoke(delegate, args);

        System.out.println("proxy after...");


        return result;
    }
}
