package com.bruin.jvm;

/**
 * @description:
 *  重载方法名和参数的限制可以通过字节码工具绕开。也就是说，在编译完成之后，我们可以再向class文件中添加方法名和参数类型相同，
 *  而返回类型不同的方法。当这种包括多个方法名相同、参数类型相同，而返回类型不同的方法的类，出现在Java编译器的用户类路径上时，
 *  它是怎么确定需要调用哪个方法的呢？当前版本的Java编译器会直接选取第一个方法名以及参数类型匹配的方法。
 *  并且，它会根据所选取方法的返回类型来决定可不可以通过编译，以及需不需要进行值转换等。
 * @author: xiongwenwen   2020/1/2 15:57
 */
public class InvokeMethod {
    public void invoke(Object obj, Object ... args){
        System.out.println("method1");
    }

    public void invoke(String s, Object obj, Object ... args){
        System.out.println("method2");
    }

    private static void test(String str){
        System.out.println("ParentClass");
    }
}
