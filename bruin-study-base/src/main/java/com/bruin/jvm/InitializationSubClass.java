package com.bruin.jvm;

/**
 * @description:
 * @author: xiongwenwen   2020/1/2 12:23
 */
public class InitializationSubClass extends InitializationSequence {
    public static final String finalField = "常量";
    public static String staticField = "子类静态变量";

    public String field = "子类变量";

    static{
        System.out.println(staticField);
        System.out.println("子类静态初始块");
    }

    {
        System.out.println(field);
        System.out.println("子类初始块");
    }

    public InitializationSubClass(){
        System.out.println("子类构造器");
    }

    public static void staticMethod(){
        System.out.println("子类静态方法");
    }
}
