package com.bruin.jmm;

/**
 * @description:
 * @author: xiongwenwen
 * @date: 2020/8/26 15:27
 */
public class Test {
    public static void main(String[] args) throws Exception {
        VolatileTest vt = new VolatileTest();

        Thread t1 = new Thread(vt::addVolatile);

        Thread t2 = new Thread(vt::addVolatile);

        t1.start();
        t2.start();

        t1.join();
        t2.join();

        System.out.println(vt.getVolatile());
    }
}
