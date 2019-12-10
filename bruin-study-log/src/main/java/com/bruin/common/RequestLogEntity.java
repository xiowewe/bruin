package com.bruin.common;

import lombok.Data;

import java.io.Serializable;

/**
 * @description:
 * @author: xiongwenwen   2019/12/10 17:52
 */
@Data
public class RequestLogEntity implements Serializable {
    /**
     * id
     */
    private String id;

    /**
     * 流水号
     */
    private String runningAccount;

    /**
     * 请求url
     */
    private String url;

    /**
     * 请求报文头
     */
    private String header;

    /**
     * 请求参数
     */
    private String request;

    /**
     * 请求响应
     */
    private String response;

    /**
     * 请求名
     */
    private String inputName;

    /**
     * 记录创建时间
     */
    private String createTimeStr;

    /**
     * 记录创建时间
     */
    private Long createTimeLong;

    /**
     * 处理机器的IP
     */
    private String ip;

    /**
     * 线程名
     */
    private String threadName;

    /**
     * 耗时
     */
    private Long useTime;
}
