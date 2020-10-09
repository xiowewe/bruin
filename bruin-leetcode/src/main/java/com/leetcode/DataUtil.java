package com.leetcode;

/**
 * @description:
 * @author: xiongwenwen
 * @date: 2020/9/24 16:29
 */
public class DataUtil {

    public static void swap(int a,int b){
        int temp = a;
        a = b;
        b = temp;
    }

    public static void printAll(int[] items){
        for (int i = 0; i < items.length; i++) {
            System.out.print(items[i] + "，");
        }
        System.out.println();
    }
}
