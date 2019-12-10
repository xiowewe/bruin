package com.bruin.common;

import java.util.UUID;

/**
 * @description:
 * @author: xiongwenwen   2019/12/10 17:35
 */
public class ThreadLocalHolder {
    private static ThreadLocal<ThreadHolder> contextHolder = new ThreadLocal<>();

    public ThreadLocalHolder() {
    }

    public static void initRunningAccount(){
        ThreadHolder th = contextHolder.get();

        if(null == th){
            th = new ThreadHolder();
            contextHolder.set(th);
        }

        th.setRunningAccountId(UUID.randomUUID().toString());
    }

    public static String getRunningAccountId(){
        if(null == contextHolder.get()){
            initRunningAccount();
        }

        return contextHolder.get().getRunningAccountId();
    }


    public static Integer getRunningAccountFlag(){
        if(null == contextHolder.get()){
            initRunningAccount();
        }

        return contextHolder.get().getRunningAccountFlag();
    }

    public static void remove(){
        if(contextHolder!=null){
            contextHolder.remove();
        }
    }
}
