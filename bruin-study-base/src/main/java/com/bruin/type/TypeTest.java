package com.bruin.type;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @description:
 * @author: xiongwenwen   2019/12/31 15:14
 */
public class TypeTest {
    public static void main(String[] args) {
        List<String> strings = new ArrayList<>();
        List<Integer> integers = new ArrayList<>();

        if(strings.equals(integers)){
            System.out.println("the same type");
        }
    }
}
