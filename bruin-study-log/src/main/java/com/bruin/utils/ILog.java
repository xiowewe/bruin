package com.bruin.utils;

/**
 * @description:
 * @author: xiongwenwen   2019/12/10 17:18
 */
public interface ILog {

    void sendInfoLog(Object... objs);

    void sendErrorLog(Object... objs);

    void sendBeginRequestLog(Object obj);

    void sendEndRequestLog(Object obj);

    boolean isNeedLog();
}
