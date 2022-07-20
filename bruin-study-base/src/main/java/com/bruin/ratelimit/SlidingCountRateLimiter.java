package com.bruin.ratelimiter;

import lombok.Data;

/**
 * @author xiongwenwen
 * @since 2022/7/20 2:40 下午
 */
public class SlidingCountRateLimiter {
    /** 单位时间分割多少块 */
    private int slot;
    /** 每个slot的时间段 */
    private long slotTime;
    /** 单位时间限制的次数 */
    private long limit;
    private long windowTime;
    /** 记录窗口滑动倒的Node */
    private Node lastNode;

    private void init() {
        // 环形链表
        Node currentNode = null;
        long current = System.currentTimeMillis();
        for (int i = 0; i < slot; i++) {
            if (lastNode == null) {
                lastNode = new Node(current, 0, i + 1);
                currentNode = lastNode;
            } else {
              lastNode.next = new Node(current, 0, i + 1);
              lastNode = lastNode.next;
            }
        }
        lastNode.next = currentNode;

        slotTime = windowTime / slot;
    }

    public synchronized boolean tryAcquire() {
        // 窗口滑动，重置lastNode
        reset();
        long sum = getSum();
        System.out.println(sum);

        // 累计slot的counter超限则拒绝
        if (sum >= limit) {
            return false;
        }
        // 最后一个子窗口 +1
        lastNode.addCounter();
        return true;
    }

    /**
     * 窗口滑动，重置lastNode
     */
    private void reset() {
        long currentTimeMillis = System.currentTimeMillis();
        long time = lastNode.getTime();
        int count = (int) ((currentTimeMillis - time) / slotTime);
        if (count > slot) {
            count = slot;
        }
        reset(count, currentTimeMillis);
    }

    private void reset(int count, long currentTimeMillis) {
        if (count <= 0) {
            return;
        }
        Node currentNode = lastNode;
        for (int i = 0; i < count; i++) {
            currentNode = currentNode.next;
        }
        currentNode.setTime(currentTimeMillis);
        currentNode.setCounter(0L);
        lastNode = currentNode;
    }

    /**
     * 累加每个slot的counter
     * @return
     */
    private long getSum() {
        long sum = 0L;
        Node currentNode = lastNode;
        for (int i = 0; i < slot; i++) {
            sum += currentNode.counter;
            currentNode = currentNode.next;
        }
        return sum;
    }

    @Data
    class Node {
        private long time;
        private long counter;
        private Node next;
        private int id;

        public Node(long time, long counter, int id) {
            this.time = time;
            this.counter = counter;
            this.id = id;
        }

        public void addCounter() {
            this.counter = counter + 1;
        }
    }
}

