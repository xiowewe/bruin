package com.bruin.common;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

@Data
public class LogObject {
    @JSONField(ordinal = 1)
    private String eventName;

    @JSONField(ordinal = 2)
    private String msg;

    @JSONField(ordinal = 3)
    private long costTime;

    @JSONField(ordinal = 4)
    private Object request;

    @JSONField(ordinal = 5)
    private Object response;

    @JSONField(ordinal = 6)
    private String ip;
}
