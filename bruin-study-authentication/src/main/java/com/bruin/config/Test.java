package com.bruin.config;

import java.util.UUID;

/**
 * @description:
 * @author: xiongwenwen   2019/12/3 15:28
 */
public class Test {


    public static void test(){
        int i = 0;
        String key = UUID.randomUUID().toString();
        System.out.println(key);
        i ++;

        if(i < 5){
            test();
        }


    }

    public static void main(String[] args) {
//        test();

        System.out.println(4 & 11);
    }
}
