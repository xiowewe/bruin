package com.bruin.test;

import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.RecordMetadata;

/**
 * @description:
 * @author: xiongwenwen   2019/12/21 15:34
 */
public class SyncProducerCallback implements Callback {
    @Override
    public void onCompletion(RecordMetadata recordMetadata, Exception e) {

        System.out.println("do something");

        if(null != e){
            e.printStackTrace();
        }
    }
}
