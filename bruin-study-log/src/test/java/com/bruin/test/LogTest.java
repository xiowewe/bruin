package com.bruin.test;

import com.bruin.common.ThreadLocalHolder;
import com.bruin.service.LogService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @description:
 * @author: xiongwenwen   2019/12/10 18:22
 */
@SpringBootTest
public class LogTest {

    @Autowired
    private LogService logService;

    @Test
    public void test(){
        for (int i = 0; i < 100; i++) {

            for (int j = 0; j < 100; j++) {
                ThreadLocalHolder.getRunningAccountId();
                ThreadLocalHolder.remove();
            }
        }
    }
}
