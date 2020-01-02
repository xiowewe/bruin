package com.bruin.jvm;

/**
 * @description: jvm初始话顺序
 *  1.类从顶至底的顺序初始化，所以声明在顶部的字段的早于底部的字段初始化
 *  2.超类早于子类和衍生类的初始化
 *  3.如果类的初始化是由于访问静态域而触发，那么只有声明静态域的类才被初始化，而不会触发超类的初始化或者子类的
 *  4.初始化即使静态域被子类或子接口或者它的实现类所引用。
 *  5.接口初始化不会导致父接口的初始化。
 *  6.静态域的初始化是在类的静态初始化期间，非静态域的初始化时在类的实例创建期间。这意味这静态域初始化在非静态域之前。
 *  7.非静态域通过构造器初始化，子类在做任何初始化之前构造器会隐含地调用父类的构造器，他保证了非静态或实例变量（父类）初始化早于子类
 *  8.JDK7之后常量池放到了heap中，引用到了定义常量的类，因而会初始化类
 * @author: xiongwenwen   2020/1/2 11:58
 */
public class InitializationSequence {

    public static String staticField = "静态变量";

    public String field = "变量";

    static{
        System.out.println(staticField);
        System.out.println("静态初始块");
    }

    {
        System.out.println(field);
        System.out.println("初始块");
    }

    public InitializationSequence(){
        System.out.println("构造器");
    }

    public static void staticMethod(){
        System.out.println("静态方法");
    }




    static class Parent{
        public static int a = 1;

        static{
            a = 2;
        }
    }

    static class Son extends Parent{
        public static int b = a;
    }
}
