package com.bruin.common;

import lombok.Data;
import lombok.NonNull;

/**
 * @description: jedis 配置信息类
 * @author: xiongwenwen   2019/12/12 16:05
 */
@Data
public class JedisProperty {
    @NonNull
    private String host;
    @NonNull
    private String port;
    @NonNull
    private String password;
    private String maxTotal;
    private String maxIdle;
    private String minIdle;
}
