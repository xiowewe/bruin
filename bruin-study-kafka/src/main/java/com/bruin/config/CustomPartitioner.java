package com.bruin.config;

import org.apache.kafka.clients.producer.Partitioner;
import org.apache.kafka.common.Cluster;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.record.InvalidRecordException;
import org.apache.kafka.common.utils.Utils;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;
import java.util.Map;

/**
 * @description: 自定义分区器
 * @author: xiongwenwen   2019/12/21 12:15
 */
public class CustomPartitioner implements Partitioner {

    @Value("custom-partitioner")
    private String customPartitioner = "test";

    @Override
    public int partition(String topic, Object key, byte[] keyBytes, Object value, byte[] valueBytes, Cluster cluster) {
        List<PartitionInfo> partitions = cluster.partitionsForTopic(topic);
        int numsPartition = partitions.size();
        if(keyBytes == null || !(key instanceof String)){
            throw new InvalidRecordException("we expect all messages to hava customer name as key");
        }

        //总是分配在最后一个区
        if(customPartitioner.equals(key)){
            return numsPartition;
        }

        //散列到其他分区
        return (Math.abs(Utils.murmur2(keyBytes)) % (numsPartition - 1));
    }

    @Override
    public void close() {

    }

    @Override
    public void configure(Map<String, ?> map) {

    }
}
