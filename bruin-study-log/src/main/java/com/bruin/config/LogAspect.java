package com.bruin.config;

import com.alibaba.fastjson.JSON;
import com.bruin.common.RequestLogEntity;
import com.bruin.common.ThreadLocalHolder;
import com.bruin.utils.ILog;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @description: serviceimpl log打印切片
 * @author: xiongwenwen   2019/12/10 17:48
 */
@Component
@Aspect
public class LogAspect {

    @Autowired
    private ILog log;

    private final String LINE_SPE = "||";

    @Around("execution(* com.bruin.service.impl.*.*(..))")
    public Object aroundMethod(ProceedingJoinPoint pjp) throws Throwable {
        return doinvoke(pjp);
    }

    private Object doinvoke(ProceedingJoinPoint pjp) throws Throwable {
        // 获取信息
        long beginTime = System.currentTimeMillis();
        Object[] args = pjp.getArgs();
        String methodName = pjp.getSignature().toString();

        // 新建request对象
        RequestLogEntity requestLogEntity = null;
        Object returnValue = null;

        ThreadLocalHolder.initRunningAccount();
        String runningAccount = ThreadLocalHolder.getRunningAccountId();
        try {
            // 看看是不是要日志
            if (log.isNeedLog()) {
                // 获取参数
                requestLogEntity = new RequestLogEntity();
                requestLogEntity.setUrl(methodName);
                requestLogEntity.setHeader("header:LogAspect");
                StringBuilder argSB = new StringBuilder(4048);
                for (Object arg : args) {
                    argSB.append(arg + ":");
                    argSB.append(JSON.toJSONString(arg));
                    argSB.append(LINE_SPE);
                }
                requestLogEntity.setRequest(argSB.toString());
                log.sendBeginRequestLog(requestLogEntity);
            }
            if (returnValue == null) {
                returnValue = pjp.proceed();
            }
        } catch (Exception e) {
            throw e;
        } finally {
            if (log.isNeedLog()) {
                long endTime = System.currentTimeMillis();
                requestLogEntity.setResponse("RA:" + runningAccount + " " + "Method [" + methodName + "] " + LINE_SPE + "returned [" + JSON.toJSONString(returnValue) + "]" + "useTime:" + (endTime - beginTime));
                requestLogEntity.setUseTime(endTime - beginTime);
                log.sendEndRequestLog(requestLogEntity);
            }
        }
        return returnValue;
    }
}
