package com.leetcode.stack;

/**
 * @description: 自实现数组栈
 * @author: xiongwenwen
 * @date: 2020/9/23 15:30
 */
public class ArrayStack {
    private String[] items;
    private int count;
    private int size;

    public ArrayStack(int n) {
        this.items = new String[n];
        this.count = 0;
        this.size = n;
    }

    public boolean push(String item){
        if(count >= size){
            return false;
        }
        items[count] = item;
        count ++;
        return true;
    }

    public String pop(){
        if(count <= 0){
            return null;
        }
        String item = items[count - 1];
        count --;
        return item;
    }

    public void itemsPrint(){
        for (int i = 0; i < count; i++) {
            System.out.println(items[i]);
        }
    }
}
