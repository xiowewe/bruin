package com.bruin.utils;

import java.util.UUID;

/**
 * @description:
 * @author: xiongwenwen   2019/12/7 15:31
 */
public class UUIDGenerator {

    private UUIDGenerator(){}

    /**
     * 获取uuid
     * @return
     */
    public static String getUUID(){

        return UUID.randomUUID().toString().replace("-","");
    }

    /**
     * 批量获取number个uuid
     * @param number
     * @return
     */
    public static String[] getUUID(int number) {
        if (number < 1) {
            return null;
        }
        String[] ss = new String[number];
        for (int i = 0; i < number; i++) {
            ss[i] = getUUID();
        }
        return ss;
    }
}
