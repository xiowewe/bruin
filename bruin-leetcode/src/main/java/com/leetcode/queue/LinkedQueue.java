package com.leetcode.queue;

/**
 * @description: 自实现链表队列
 * @author: xiongwenwen
 * @date: 2020/9/24 11:22
 */
public class LinkedQueue {
    private String[] items;
    private int count;
    private int size;

    public LinkedQueue(int n) {
        this.items = new String[n];
        this.count = n - 1;
        this.size = n;
    }

    public boolean offer(String item){
        if(count < 0){
            return false;
        }
        items[count --] = item;
        return true;
    }

    public String poll(){
        return items[count --];
    }

    public void printAll(){
        for (int i = size - 1; i > 0; i --) {
            System.out.println(items[i]);
        }
    }

    public static void main(String[] args) {
        LinkedQueue queue = new LinkedQueue(5);
        for (int i = 0; i <= 6; i++) {
            System.out.println(queue.offer(i + ""));
        }


        queue.printAll();
    }
}
