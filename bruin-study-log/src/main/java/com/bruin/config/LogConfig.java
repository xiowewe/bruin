package com.bruin.config;

import com.bruin.utils.ILog;
import com.bruin.utils.Log;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @description:
 * @author: xiongwenwen   2019/12/10 17:48
 */
@Configuration
public class LogConfig {

    @Bean
    public ILog init(){
        return new Log();
    }
}
