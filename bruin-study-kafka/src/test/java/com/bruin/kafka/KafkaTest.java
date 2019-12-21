package com.bruin.kafka;

import com.bruin.config.KafkaProvider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @description:
 * @author: xiongwenwen   2019/12/20 14:51
 */
@SpringBootTest
public class KafkaTest {

    @Autowired
    private KafkaProvider kafkaProvider;

    @Test
    public void testSend(){

        kafkaProvider.sendMessage("test_topic", "test");

    }
}
