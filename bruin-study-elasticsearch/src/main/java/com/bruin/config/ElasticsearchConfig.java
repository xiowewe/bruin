package com.bruin.config;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @description:
 * @author: xiongwenwen   2019/11/25 14:19
 */
@Configuration
public class ElasticsearchConfig {

    private String hostName = "192.168.159";

    @Bean
    public RestHighLevelClient elasticSearchClient(){
        return new RestHighLevelClient(
                RestClient.builder(new HttpHost(hostName, 9200, "http")));
    }
}
