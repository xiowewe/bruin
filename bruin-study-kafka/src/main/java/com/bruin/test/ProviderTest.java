package com.bruin.test;

import com.alibaba.fastjson.JSON;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;
import java.util.concurrent.ExecutionException;

/**
 * @description:
 * @author: xiongwenwen   2019/12/21 11:09
 */
public class ProviderTest {

    private static final Logger logger = LoggerFactory.getLogger(ProviderTest.class);

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        Properties kafkaProps = new Properties();
        kafkaProps.put("bootstrap.servers","192.168.159.130:9092");
        kafkaProps.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        kafkaProps.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
//        kafkaProps.put("partitioner.class", "com.bruin.config.CustomPartitioner");

        KafkaProducer<String, String> producer = new KafkaProducer<String, String>(kafkaProps);

        for (int i = 0; i < 10; i++) {
            ProducerRecord<String, String> record = new ProducerRecord<>("test_topic", "test-key","第" + i + "个消息");

            RecordMetadata recordMetadata = producer.send(record).get();

            logger.info("消息结果：{}",JSON.toJSONString(recordMetadata));
        }

        /*异步消息*/
//        producer.send(record, new SyncProducerCallback());
    }

}
