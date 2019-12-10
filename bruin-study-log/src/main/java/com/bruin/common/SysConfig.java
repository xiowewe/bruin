package com.bruin.common;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @description:
 * @author: xiongwenwen   2019/12/10 17:28
 */
public class SysConfig {
    private static Map<String, String> sysConfig = new ConcurrentHashMap<>();


    public static void add(String key, String value){
        sysConfig.put(key, value);
    }

    public static String getValue(String key){
        return sysConfig.get(key);
    }

    public static String getDefaultValue(String key, String defaultValue){
        return sysConfig.getOrDefault(key, defaultValue);
    }


    public static Map<String, String> getSysConfig() {
        return sysConfig;
    }

    public static void setSysConfig(Map<String, String> sysConfig) {
        SysConfig.sysConfig = sysConfig;
    }
}
