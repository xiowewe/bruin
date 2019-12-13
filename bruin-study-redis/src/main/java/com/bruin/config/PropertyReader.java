package com.bruin.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @description: 读取jedis配置信息
 * @author: xiongwenwen   2019/12/12 15:52
 */
@Data
@Component
@ConfigurationProperties(prefix = "spring.redis")
public class PropertyReader {
    private String host;
    private String port;
    private String password;
    private String maxTotal;
    private String maxIdle;
    private String minIdle;
}
