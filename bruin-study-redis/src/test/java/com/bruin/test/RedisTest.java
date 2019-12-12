package com.bruin.test;

import com.bruin.utils.RedisJedisUtil;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @description:
 * @author: xiongwenwen   2019/12/12 16:56
 */
@SpringBootTest
public class RedisTest {

    @Test
    public void test(){
        for (int i = 0; i < 100; i++) {
            for (int j = 0; j < 100; j++) {
                RedisJedisUtil.set(i + "", "test");
            }
        }
    }
}
