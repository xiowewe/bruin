package com.bruin.jmm;

/**
 * @description:
 * @author: xiongwenwen
 * @date: 2020/8/26 15:25
 */
public class VolatileTest {
    private volatile int i = 0;

    public int getVolatile(){
        return i;
    }

    public void addVolatile() {
        for (int j = 0; j < 1000; j++) {
            i++;
        }
    }
}
