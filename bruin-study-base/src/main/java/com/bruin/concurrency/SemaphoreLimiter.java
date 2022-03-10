package com.bruin.concurrency;

import java.util.List;
import java.util.Vector;
import java.util.concurrent.Semaphore;
import java.util.function.Function;

/**
 * @author xiongwenwen
 * @since 2022/3/10 8:26 下午
 */
public class SemaphoreLimiter<T, R> {
    private final Semaphore sem;
    private final List<T> pool;

    public SemaphoreLimiter(int limit, T t) {
        pool = new Vector<T>();
        sem = new Semaphore(limit);
        for(int i = 0; i < limit; i++){
            pool.add(t);
        }
    }

    public R exec(Function<T, R> func) throws InterruptedException {
        T t = null;
        sem.acquire();
        try{
            t = pool.remove(0);
            return func.apply(t);
        }finally {
            pool.add(t);
            sem.release();
        }
    }

    public static void main(String[] args) throws InterruptedException {
        SemaphoreLimiter<String, String> pool = new SemaphoreLimiter<>(3, "do someThing");
        for (int i = 0; i < 15; i++) {
            int finalI = i;
            new Thread(() ->{
                try {
                    pool.exec(t ->{
                        System.out.println(t + ":" + finalI);
                        try {
                            int sleep = finalI%3 == 0 ? 3 : finalI%3;
                            Thread.sleep(sleep * 1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        return t;
                    });
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        }

    }
}
