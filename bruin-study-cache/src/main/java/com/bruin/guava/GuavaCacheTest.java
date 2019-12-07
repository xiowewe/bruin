package com.bruin.guava;

import com.google.common.cache.*;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListenableFutureTask;

import java.util.concurrent.*;

/**
 * @description:
 * @author: xiongwenwen   2019/11/21 10:23
 */
public class GuavaCacheTest {

    public static void main(String[] args) {
        LoadingCache<Integer, Integer> graphs = guavaCacheLoader();

        try {
            System.out.println(graphs.get(4));
            graphs.put(1, 2);
            System.out.println(graphs.get(1));

            graphs.invalidate(1);
            graphs.stats();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }


        Cache<Integer, Integer> cache = guavaCache();
        try {
            // 获取某个key时，在Cache.get中单独为其指定load方法
            Integer value = cache.get(1, new Callable<Integer>() {
                @Override
                public Integer call() throws Exception {
                    return 2;
                }
            });

        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    public static LoadingCache<Integer, Integer> guavaCacheLoader(){
        LoadingCache<Integer, Integer> graphs = CacheBuilder.newBuilder()
                .maximumSize(100)
                .weigher(new Weigher<Integer, Integer>() {
                    @Override
                    public int weigh(Integer integer, Integer integer2) {
                        return 0;
                    }
                })
                .expireAfterAccess(100, TimeUnit.SECONDS)
                .refreshAfterWrite(1, TimeUnit.MINUTES)
                .recordStats()
                .build(
                        new CacheLoader<Integer, Integer>() {
                            ExecutorService executorService = Executors.newFixedThreadPool(10);

                            @Override
                            public Integer load(Integer key) throws Exception {
                                //默认计算与键关联的值
                                return key * key;
                            }

                            @Override
                            public ListenableFuture<Integer> reload(final Integer key, final Integer oldValue) {
                                ListenableFutureTask<Integer> task = ListenableFutureTask.create(new Callable<Integer>() {
                                    @Override
                                    public Integer call() throws Exception {
                                        return key * key * key;
                                    }
                                });
                                executorService.execute(task);
                                return task;
                            }
                        }


                );

        return graphs;
    }


    public static Cache<Integer, Integer> guavaCache(){
        Cache<Integer, Integer> graphs = CacheBuilder.newBuilder()
                .maximumSize(100)
                .build();

        return graphs;
    }
}
