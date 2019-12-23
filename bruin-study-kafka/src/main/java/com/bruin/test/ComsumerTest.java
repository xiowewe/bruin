package com.bruin.test;

import com.alibaba.fastjson.JSON;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @description:
 * @author: xiongwenwen   2019/12/21 15:35
 */
public class ComsumerTest {
    private static final Logger logger = LoggerFactory.getLogger(ComsumerTest.class);

    public static void main(String[] args) {
        Properties kafkaProps = new Properties();
        kafkaProps.put("bootstrap.servers","192.168.159.130:9092");
        kafkaProps.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        kafkaProps.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        kafkaProps.put("group.id","test.group");

        KafkaConsumer<String, String> consumer = new KafkaConsumer<String, String>(kafkaProps);

        consumer.subscribe(Collections.singleton("test_topic"));

        Map<String, Integer> valueMap = new HashMap<>();
        try {
            while (true) {
                ConsumerRecords<String, String> records = consumer.poll(100);
                for(ConsumerRecord<String, String> record : records){
                    logger.info("topic:{},partition:{},offset:{},key:{},value:{}", record.topic(),
                            record.partition(), record.offset(), record.key(), record.value());

                    int updateCount = 1;
                    if(valueMap.containsValue(record.value())){
                        updateCount = valueMap.get(record.value()) + 1;
                    }
                    valueMap.put(record.value(), updateCount);

                    System.out.println(JSON.toJSONString(valueMap));
                }

                //手动提交偏移量
                try {
                    consumer.commitAsync();
                } catch (Exception e) {
                    logger.info("erro:{}", e.getMessage());
                }
            }
        } finally {
            consumer.close();
        }
    }

}
