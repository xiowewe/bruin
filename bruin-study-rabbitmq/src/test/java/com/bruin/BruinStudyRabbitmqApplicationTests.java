package com.bruin;

import com.bruin.config.RabbitmqConfig;
import com.bruin.provider.RabbitmqProvider;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class BruinStudyRabbitmqApplicationTests {

    @Test
    void directMsg() {

        try {

            RabbitmqProvider.sendDirectMsg(RabbitmqConfig.DIRECT_EXCHANGE, "", RabbitmqConfig.DIRECT_QUEUE, "test");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
