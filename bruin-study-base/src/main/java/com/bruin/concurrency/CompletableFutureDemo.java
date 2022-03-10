package com.bruin.concurrency;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author xiongwenwen
 * @since 2022/3/1 3:14 下午
 */
public class CompletableFutureDemo {
    static final ExecutorService pool = new ThreadPoolExecutor(10,
            10,
            0,
            TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<>(),
            new NameThreadFactory());

    public static void main(String[] args) {
        CompletableFuture<String> future = CompletableFuture.supplyAsync(() ->{
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            return "hello CompletableFuture";
        }).thenApplyAsync((s) ->{
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println(s);
            System.out.println("thenApplyAsync doing!");
            return "thenApplyAsync";
        }).thenComposeAsync(c -> CompletableFuture.supplyAsync(() ->{
            System.out.println("thenComposeAsync doing");
            return c + "----" + "thenComposeAsync";
        })).handleAsync((v, e) ->{
            if(null != e){
                System.out.println(e.getMessage());
                pool.shutdown();
            }
            return "result:" + v;
        });


        System.out.println("before");
        try {
            System.out.println(future.get());
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            pool.shutdown();
        }
        System.out.println("after");

    }

    private static CompletableFuture<String> otherCompletableFuture(){
        return CompletableFuture.supplyAsync(() ->{
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            return "other CompletableFuture doing";
        });
    }
}

class NameThreadFactory implements ThreadFactory{

    @Override
    public Thread newThread(Runnable r) {
        return new Thread(r,"customer-thread-pool");
    }
}

