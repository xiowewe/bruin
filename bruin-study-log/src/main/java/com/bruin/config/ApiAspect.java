package com.bruin.config;

import com.alibaba.fastjson.JSON;
import com.bruin.common.LogObject;
import com.bruin.utils.RequestUtil;
import org.apache.logging.log4j.ThreadContext;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.util.UUID;

/**
 * @description:
 * @author: xiongwenwen   2019/12/11 14:30
 */
@Aspect
@Component
public class ApiAspect {

    private static Logger logger = LoggerFactory.getLogger(ApiAspect.class);

    private static final String LINE_SPE = "|";

    @Around("execution(* com.bruin.controller.*.*(..))")
    public Object around(ProceedingJoinPoint pjp) throws Throwable{
        long beginTime = System.currentTimeMillis();
        MethodSignature signature = (MethodSignature) pjp.getSignature();
        Method method = signature.getMethod();
        String methodName = method.toString();

        Object result = null;
        LogObject logObject = null;
        String msg = "success";

        ThreadContext.put("logId", UUID.randomUUID().toString());

        try {
            String url = RequestUtil.getCurrentRequest().getRequestURI();

            Object[] args = pjp.getArgs();
            StringBuffer argSB = new StringBuffer(2048);
            for (Object arg : args) {
                if(arg instanceof HttpServletRequest || arg instanceof HttpServletResponse){
                    continue;
                }

                try {
                    argSB.append(JSON.toJSONString(arg));
                } catch (Exception e) {
                    logger.error("{} 入参转化异常 {}", url, JSON.toJSONString(arg), e);
                }

                argSB.append(LINE_SPE);
            }

            logObject = new LogObject();
            logObject.setEventName(url);
            logObject.setRequest(argSB.toString());
            logObject.setIp(RequestUtil.getIP());

            result = pjp.proceed();

        } catch (Exception e) {
            msg = e.getMessage();
            logger.error("{}出错,request:{}", methodName, JSON.toJSONString(logObject), e);
            throw e;
        }finally {
            long endTime = System.currentTimeMillis();

            logObject.setMsg(msg);
            logObject.setResponse(result);
            logObject.setCostTime(endTime - beginTime);
            if(logObject.getCostTime() > 1000L){
                logger.info("接口响应慢 耗时{}ms  {}", logObject.getCostTime(), JSON.toJSONString(logObject));
            }else{
                logger.info("logObject:{}", JSON.toJSONString(logObject));
            }

            ThreadContext.clearStack();
        }

        return result;
    }
}
