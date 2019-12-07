package com.bruin.config;

import com.google.common.cache.*;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListenableFutureTask;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @description:
 * @author: xiongwenwen   2019/11/21 16:42
 */
@Component
public class GuavaCacheConfig {


    @Bean
    public LoadingCache<Integer, Integer> loadingCache(){
        LoadingCache<Integer, Integer> graphs = CacheBuilder.newBuilder()
                .maximumSize(10)
                /*.weigher(new Weigher<Integer, Integer>() {
                    @Override
                    public int weigh(Integer integer, Integer integer2) {
                        return 0;
                    }
                })*/
                .expireAfterAccess(100, TimeUnit.SECONDS)
                .refreshAfterWrite(1, TimeUnit.MINUTES)
                .recordStats()
                .build(
                        new CacheLoader<Integer, Integer>() {
                            ExecutorService executorService = Executors.newFixedThreadPool(10);

                            @Override
                            public Integer load(Integer key) throws Exception {
                                //默认计算与键关联的值
                                return loadVaule(key);
                            }

                            @Override
                            public ListenableFuture<Integer> reload(final Integer key, final Integer oldValue) {
                                ListenableFutureTask<Integer> task = ListenableFutureTask.create(new Callable<Integer>() {
                                    @Override
                                    public Integer call() throws Exception {
                                        return reLoadVaule(key);
                                    }
                                });
                                executorService.execute(task);
                                return task;
                            }
                        }


                );

        return graphs;
    }

    @Bean
    public Cache<Integer, Integer> cache(){
        Cache<Integer, Integer> graphs = CacheBuilder.newBuilder()
                .maximumSize(100)
                .build();

        return graphs;
    }

    private Integer loadVaule(Integer key){
        return key * key;
    }

    private Integer reLoadVaule(Integer key){
        return key * key * key;
    }
}
