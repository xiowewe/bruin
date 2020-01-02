package com.bruin.reflect;

/**
 * @description:
 * @author: xiongwenwen   2019/12/31 14:28
 */
public class PrivateTest {
    private String str = "private";
    private final String finalValue;

    public PrivateTest(){
        this.finalValue = "final";
    }

    private void privateMethod(String head, int tail){
        System.out.println(head + tail);
    }

    public String getStr(){
        return str;
    }

    public String getFinalValue(){
        return finalValue;
    }
}
