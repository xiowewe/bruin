package com.bruin.concurrency;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author xiongwenwen
 * @since 2022/3/9 3:44 下午
 */
public class SynchronizedOrLockBlockedQueue {
    public static void main(String[] args) {
        ExecutorService executorService = Executors.newFixedThreadPool(5);
        SynchronizedBlockQueue<String> synchronizedBlockQueue = new SynchronizedBlockQueue<>(10);
        ConditionBlockedQueue<String> conditionBlockedQueue = new ConditionBlockedQueue<>(10);
        //入队
        executorService.submit(() ->{
            while (true){
                conditionBlockedQueue.enq(new Random().nextInt() + "");
            }
        });

        //出队
        executorService.submit(() ->{
            while (true){
                conditionBlockedQueue.deq();
            }
        });
    }
}

class ConditionBlockedQueue<T>{
    private final Lock lock = new ReentrantLock();
    private final Condition notNull = lock.newCondition();
    private final Condition notFull = lock.newCondition();

    private final int capital;
    private final List<T> container;
    private volatile int size;

    public ConditionBlockedQueue(int capital) {
        this.capital = capital;
        this.container = new ArrayList<>(capital);
    }

    public void enq(final T e){
        lock.lock();
        try {
            while (size >= capital) {
                try {
                    System.out.println("ConditionBlockedQueue 队列已满");
                    notFull.await();
                } catch (InterruptedException interruptedException) {
                    System.out.println("ConditionBlockedQueue error");
                    notFull.signalAll();
                }
            }
            System.out.println(size + "加入ConditionBlockedQueue");
            size++;
            container.add(e);
            notNull.signalAll();
        }finally {
            lock.unlock();
        }
    }

    public void deq(){
        lock.lock();
        try {
            while(size == 0){
                try {
                    System.out.println("ConditionBlockedQueue 队列已空");
                    notNull.await();
                } catch (InterruptedException interruptedException) {
                    System.out.println("ConditionBlockedQueue error");
                    notNull.signalAll();
                }
            }
            System.out.println(size + "取出ConditionBlockedQueue");
            size--;
            container.remove(0);
            notFull.signalAll();
        }finally {
            lock.unlock();
        }
    }

}

class SynchronizedBlockQueue<T>{
    private Object lock = new Object();
    private final int capital;
    private final List<T> container;
    private volatile int size;

    public SynchronizedBlockQueue(int capital) {
        this.capital = capital;
        this.container = new ArrayList<>(capital);
    }

    public void enq(final T e){
        synchronized (lock){
            while(size >= capital){
                try {
                    System.out.println("SynchronizedBlockQueue 队列已满");
                    lock.wait();
                } catch (InterruptedException interruptedException) {
                    System.out.println("SynchronizedBlockQueue error");
                    lock.notifyAll();
                }
            }
            System.out.println(size + "加入SynchronizedBlockQueue");
            size++;
            container.add(e);
            lock.notifyAll();
        }
    }

    public void deq(){
        synchronized (lock){
            while(size == 0){
                try {
                    System.out.println("SynchronizedBlockQueue 队列已空");
                    lock.wait();
                } catch (InterruptedException interruptedException) {
                    System.out.println("SynchronizedBlockQueue error");
                    lock.notifyAll();
                }
            }
            System.out.println(size + "取出SynchronizedBlockQueue");
            size--;
            container.remove(0);
            lock.notifyAll();
        }
    }
}
