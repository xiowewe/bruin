package com.bruin.test;

import com.alibaba.fastjson.JSON;
import com.bruin.config.PropertyReader;
import com.bruin.utils.RedisJedisUtil;
import com.bruin.utils.RedisUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @description:
 * @author: xiongwenwen   2019/12/12 16:56
 */
@SpringBootTest
public class RedisTest {

    @Autowired
    private PropertyReader propertyReader;
    @Autowired
    private RedisUtil redisUtil;

    @Test
    public void test1(){
        System.out.println(JSON.toJSONString(propertyReader));
    }

    @Test
    public void test(){
        redisUtil.set("redis", "test");
    }
}
