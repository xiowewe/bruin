package com.bruin.proxy;

import org.springframework.cglib.proxy.Enhancer;

/**
 * @description:
 * @author: xiongwenwen   2019/12/11 16:05
 */
public class Test {

    public static void main(String[] args) {
//        StaticPersonProxy staticProxy = new StaticPersonProxy(new Person());
//
//        staticProxy.buyHouse();

        DynamicPersonProxy dynamicProxy = new DynamicPersonProxy();
        IPerson iPerson = (IPerson) dynamicProxy.bind(new Person());

        iPerson.buyHouse();


        Person person = CglibPersonProxy.getProxyInstance();
        person.buyHouse();
    }
}
