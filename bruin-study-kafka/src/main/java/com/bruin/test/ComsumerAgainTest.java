package com.bruin.test;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collections;
import java.util.Properties;

/**
 * @description: 重新消费
 * 1. 修改offset:
 *  我们在使用consumer消费的时候，每个topic会产生一个偏移量，这个偏移量保证我们消费的消息顺序且不重复。
 *  Offest是在zookeeper中存储的，我们可以设置consumer实时或定时的注册offset到zookeeper中。
 *  我们修改这个offest到我们想重新消费的位置，就可以做到重新消费了。具体修改offest的方法这里就不详细介绍了，
 *  0.9之后得版本，offset保存在kafka服务端__consumer__offsets
 *
 * 2. 通过使用不同的group来消费
 *  通过不同的group来重新消费数据方法简单，但我们无法指定我们要重复消费哪些数据，它会从这个groupid在zookeeper注册之后
 *  所产生的数据开始消费。这里需要注意的是新的group是重新消费所有数据，但也并非是topic中所有数据，它只会消费它在zookeeper
 *  注册过之后产生的数据。我们可以再zookeeper客户端中  /consumer/  目录下查看我们已经注册过的groupid。我们在使用consumer
 *  消费数据时如果指定一个新的groupid，那么当这个consumer被执行的时候会自动注册到zookeeper中。而这个group中的consumer之后
 *  消费到注册之后产生的数据。
 * @author: xiongwenwen   2019/12/23 14:34
 */
public class ComsumerAgainTest {
    private static final Logger logger = LoggerFactory.getLogger(ComsumerAgainTest.class);

    private static final String topic = "test_topic";

    public static void main(String[] args) {

        agianConsumserTest1();
    }

    /**
     * 使用不同的组名，并且通过 consumer.assign
     */
    public static void agianConsumserTest1() {
        Properties kafkaProps = new Properties();
        kafkaProps.put("bootstrap.servers","192.168.159.130:9092");
        kafkaProps.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        kafkaProps.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        kafkaProps.put("group.id","test.group1");

        KafkaConsumer<String, String> consumer = new KafkaConsumer<String, String>(kafkaProps);

        //从0开始消费
        int offiset = 0;
        int partition = 0;
        TopicPartition topicPartition = new TopicPartition(topic, partition);

        consumer.assign(Arrays.asList(topicPartition));
        consumer.seek(new TopicPartition(topic, partition), offiset);

        while (true){
            ConsumerRecords<String, String> records = consumer.poll(100);
            for (ConsumerRecord<String, String> record : records){
                System.out.println("Received message: (" + record.key() + ", " + record.value() + ") at offset " + record.offset());
            }

            consumer.commitAsync();
        }
    }


    /**
     * 通过consumer.subscribe()指定偏移量
     */
    public static void agianConsumserTest2() {
        Properties kafkaProps = new Properties();
        kafkaProps.put("bootstrap.servers","192.168.159.130:9092");
        kafkaProps.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        kafkaProps.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        kafkaProps.put("group.id","test.group2");

        KafkaConsumer<String, String> consumer = new KafkaConsumer<String, String>(kafkaProps);

        consumer.subscribe(Collections.singleton(topic));
        consumer.poll(0);
        //从所有分区的所有偏移量开始消费
        int offset = 10;

        /*for (TopicPartition partition : consumer.assignment()) {
            consumer.seek(partition, offset);
        }*/


        //从特定分区的特定偏移量开始消费
        int partition = 0;
        TopicPartition topicPartition = new TopicPartition(topic, partition);
        consumer.seek(topicPartition, offset);

        while (true){
            ConsumerRecords<String, String> records = consumer.poll(100);
            for (ConsumerRecord<String, String> record : records){
                System.out.println("Received message: (" + record.key() + ", " + record.value() + ") at offset " + record.offset());
            }

            consumer.commitAsync();
        }
    }
}
