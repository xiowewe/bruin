package com.leetcode.linked;

/**
 * @description: 自实现双链表
 * @author: xiongwenwen
 * @date: 2020/9/23 16:00
 */
public class DoublyLinked<E> {
    private Node<E> head;
    private Node<E> tail;
    private int size;

    public DoublyLinked() {
        this.head = new Node<>(null,null,null);
        this.tail = head;
    }

    public void offer(E e){
        Node newNode = new Node(e, null, null);
        if (size == 0){
            head = newNode;
            tail = head;
            size = 1;
            return;
        }

        if(size == 1){
            tail = newNode;
            head.next = tail;
            tail.pre = head;
            size = 2;
            return;
        }

        Node tailTemp = tail;
        tail.next = newNode;
        tail = newNode;
        tail.pre = tailTemp;
        size ++;
    }

    public void popHead(){
        Node nextNode = head.next;
        head = nextNode;
        head.pre = null;
        size --;
    }

    public void popTail(){
        Node preNode = tail.pre;
        tail = preNode;
        preNode.next = null;
        size --;
    }


    public void printAllAsc(){
        if(head != null){
            System.out.println(head.e);

            Node node = head.next;
            while (node != null){
                System.out.println(node.e);
                node = node.next;
            }
        }
    }

    public void printAllDesc(){
        if(tail != null){
            System.out.println(tail.e);

            Node node = tail.pre;
            while (node != null){
                System.out.println(node.e);
                node = node.pre;
            }
        }
    }

    public int getSize() {
        return size;
    }

    private class Node<E>{
        private E e;
        private Node next;
        private Node pre;

        public Node(E e, Node next, Node pre) {
            this.e = e;
            this.next = next;
            this.pre = pre;
        }
    }

    public static void main(String[] args) {
        DoublyLinked<String> doublyLinked = new DoublyLinked<>();
        doublyLinked.offer("1");
        doublyLinked.offer("2");
        doublyLinked.offer("3");

        doublyLinked.popTail();
        doublyLinked.popHead();

        doublyLinked.printAllDesc();

        System.out.println("size:" + doublyLinked.getSize());
    }
}
