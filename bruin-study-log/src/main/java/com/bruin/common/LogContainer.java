package com.bruin.common;

import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @description:
 * @author: xiongwenwen   2019/12/10 17:24
 */
public class LogContainer {
    private static Map<String, Integer> property = new ConcurrentHashMap<>();


    public static Integer getProperty(String key){
        if(!StringUtils.isEmpty(SysConfig.getValue(key))){
            return Integer.valueOf(SysConfig.getValue(key));
        }

        return property.get(key);
    }


    public static Map<String, Integer> getProperty() {
        return property;
    }

    public static void setProperty(Map<String, Integer> property) {
        LogContainer.property = property;
    }
}
