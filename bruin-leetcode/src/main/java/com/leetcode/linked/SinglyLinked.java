package com.leetcode.linked;

/**
 * @description: 自实现单链表
 * @author: xiongwenwen
 * @date: 2020/9/23 16:00
 */
public class SinglyLinked<E> {
    private Node<E> head;
    private Node<E> tail;
    private int size;

    public SinglyLinked() {
        this.head = new Node<>(null,null);
        this.tail = head;
    }

    public void offer(E e){
        Node newNode = new Node(e, null);
        if (size == 0){
            head = newNode;
            tail = head;
            size = 1;
            return;
        }

        if(size == 1){
            tail = newNode;
            head.next = tail;
            size = 2;
            return;
        }

        tail.next = newNode;
        tail = newNode;
        size ++;
    }

    public void pop(){
        Node nextNode = head.next;
        head = nextNode;
        size --;
    }


    public void printAll(){
        if(head != null){
            System.out.println(head.e);

            Node node = head.next;
            while (node != null){
                System.out.println(node.e);
                node = node.next;
            }
        }
    }

    private class Node<E>{
        private E e;
        private Node next;

        public Node(E e, Node next) {
            this.e = e;
            this.next = next;
        }
    }

    public static void main(String[] args) {
        SinglyLinked<String> singlyLinked = new SinglyLinked<>();
        singlyLinked.offer("1");
        singlyLinked.offer("2");

        singlyLinked.pop();
        singlyLinked.offer("3");
        singlyLinked.pop();
        singlyLinked.offer("4");
        singlyLinked.offer("5");
        singlyLinked.offer("6");
        singlyLinked.pop();



        singlyLinked.printAll();
    }
}
