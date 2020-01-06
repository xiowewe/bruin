package com.bruin.jvm;

/**
 * @description:
 * @author: xiongwenwen   2020/1/2 16:08
 */
public class InvokeMethodSubClass extends InvokeMethod {

    private static void test(String str) {
        System.out.println("subclass");
    }

    public static void main(String[] args) {
        test("");
    }
}
