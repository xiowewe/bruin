package com.bruin.config;

import com.bruin.common.JedisProperty;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.context.annotation.Configuration;

/**
 * @description: 读取jedis配置信息
 * @author: xiongwenwen   2019/12/12 15:52
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "jedis-pool")
public class PropertyReader {

    @NestedConfigurationProperty
    private JedisProperty property;

}
