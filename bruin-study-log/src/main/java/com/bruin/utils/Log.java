package com.bruin.utils;

import com.alibaba.fastjson.JSON;
import com.bruin.common.LogContainer;
import com.bruin.common.ThreadLocalHolder;
import com.bruin.constant.Constant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @description:
 * @author: xiongwenwen   2019/12/10 17:19
 */
public class Log implements ILog {
    private static Logger logger = LoggerFactory.getLogger(Log.class);


    /**
     * 是否打印info日志
     * */
    @Override
    public void sendInfoLog(Object... objs) {
        if (!isNeedInfoLog()) {
            return;
        }
        StringBuilder msg = new StringBuilder(3000);
        for (Object obj : objs) {
            if (null == obj) {
                continue;
            }
            msg.append(obj2Str(obj) + Constant.LINE_SPE);
        }
        logger.info("info_log: " + ra() + msg.toString());
    }

    @Override
    public void sendErrorLog(Object... objs) {
        if (!isNeedErrorLog()) {
            return;
        }
        StringBuilder msg = new StringBuilder(2000);
        for (Object obj : objs) {
            if (null == obj) {
                continue;
            }
            msg.append(obj2Str(obj) + Constant.LINE_SPE);
        }

        logger.error("error_account_log: " + ra() + msg.toString());
    }

    @Override
    public void sendBeginRequestLog(Object obj) {
        if (!isNeedInfoLog()) {
            return;
        }
        logger.info("request_begin_log: " + ra() + obj2Str(obj));
    }

    @Override
    public void sendEndRequestLog(Object obj) {
        if (!isNeedInfoLog()) {
            return;
        }
        logger.info("request_end_log: " + ra() + obj2Str(obj));
    }

    @Override
    public boolean isNeedLog() {
        Integer flag = LogContainer.getProperty(Constant.LOG_KEY);
        //如果拿不到，不控制日志
        if (null == flag) {
            return true;
        }
        return isNeedInfoLog();
    }

    /**
     * 0,表示不需要infoLog，1,表示需要errorLog,2以上表示需要infoLog
     *
     * @return
     */

    public boolean isNeedInfoLog() {
        Integer flag = LogContainer.getProperty(Constant.LOG_KEY);
        //如果拿不到，不控制日志
        if (null == flag) {
            return true;
        }
        if (flag >= 2) {
            return true;
        }
        return false;
    }

    /**
     * 0 表示不需要log,1以上表示需要
     *
     * @return
     */
    private boolean isNeedErrorLog() {
        Integer flag = LogContainer.getProperty(Constant.LOG_KEY);
        if (null == flag) {
            return true;
        }
        return flag >= 1;
    }

    /**
     * object转字符串
     * */
    private static String obj2Str(Object income) {
        if (null == income) {
            return "";
        }

        if (income instanceof Class) {
            return ((Class) income).getName();
        }

        if (income instanceof String) {
            return income.toString();
        }

        if (income instanceof Throwable) {
//            return ExceptionUtil.getStackTrace((Throwable) income);
        }

        String jsonStr = "";
        try {
            jsonStr = JSON.toJSONString(income);
        } catch (Exception e) {
            jsonStr = income.toString();
        }
        return jsonStr;
    }
    /**
     * 日志唯一凭条
     * */
    private String ra() {
        String runningAccount = ThreadLocalHolder.getRunningAccountId();
        return "ra:" + runningAccount + ", ";
    }
}
