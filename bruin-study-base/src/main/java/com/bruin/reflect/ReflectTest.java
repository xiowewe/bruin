package com.bruin.reflect;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * @description:
 * @author: xiongwenwen   2019/12/31 14:20
 */
public class ReflectTest {
    public static void main(String[] args) throws Exception {
        PrivateTest test = new PrivateTest();

        Class tClass = test.getClass();

        Method method = tClass.getDeclaredMethod("privateMethod", String.class, int.class);
        Field field = tClass.getDeclaredField("str");
        Field finalField = tClass.getDeclaredField("finalValue");

        if(null != method){
            //访问private方法
            method.setAccessible(true);
            method.invoke(test, "refect", 123);

            //修改private 变量
            System.out.println("before change:" + test.getStr());

            field.setAccessible(true);
            field.set(test, "modified");

            System.out.println("after change:" + test.getStr());


            //修改private 常量(常量实在运行时赋值的情况)
            finalField.setAccessible(true);
            System.out.println("before:" + test.getFinalValue());

            finalField.set(test, "modified finalValue");

            System.out.println("after:" + test.getFinalValue());
        }
    }
}
