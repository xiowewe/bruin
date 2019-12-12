package com.bruin.proxy;

/**
 * @description:
 * @author: xiongwenwen   2019/12/11 16:01
 */
public class StaticPersonProxy {

    private IPerson person;

    public StaticPersonProxy(IPerson person) {
        this.person = person;
    }

    public void buyHouse(){
        System.out.println("proxy before...");

        person.buyHouse();

        System.out.println("proxy after...");
    }
}
