package com.leetcode.stack;

/**
 * @description: 自实现链表栈
 * @author: xiongwenwen
 * @date: 2020/9/24 11:22
 */
public class LinkedStack {
    private Node<Integer> head;
    private Node<Integer> tail;
    private int count;
    private int size;

    public LinkedStack(int n) {
        this.head = new Node<>(null, null);
        this.tail = head;
        this.count = 0;
        this.size = n;
    }

    public boolean push(Integer item){
        Node<Integer> node = new Node<>(null, item);
        if(count >= size){
            return false;
        }

        if(size == 0){
            head = node;
            tail = head;
        }else if(size == 1){
            tail = node;
            head.next = tail;
        }else {
            tail.next = node;
            tail = node;
        }

        count ++;
        return true;
    }

    public Integer pop(){
        Node<Integer> oldHead = head;
        Node<Integer> nextNode = head.next;
        head = nextNode;
        count --;
        return oldHead.e;
    }

    public void printAll(){
        if(head != null){
            System.out.println(head.e);

            Node<Integer> node = head.next;
            while (node != null){
                System.out.println(node.e);
                node = node.next;
            }
        }
    }

    class Node<E>{
        private Node<E> next;
        private E e;

        public Node(Node<E> next, E e) {
            this.next = next;
            this.e = e;
        }
    }

    public static void main(String[] args) {
        LinkedStack linkedStack = new LinkedStack(5);
        for (int i = 1; i <= 6; i++) {
            System.out.println(linkedStack.push(i));
        }

        System.out.println("pop:" + linkedStack.pop());


        linkedStack.printAll();
    }
}
