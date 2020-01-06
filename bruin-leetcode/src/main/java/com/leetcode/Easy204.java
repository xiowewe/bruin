package com.leetcode;

/**
 * @description: 计数质数
 * @author: xiongwenwen   2020/1/2 18:26
 */
public class Easy204 {
    public int countPrimes(int n) {
        int count = 0;
        for (int i = 2; i < n; i++) {
            if(isPrime(i)){
                count ++;
            }
        }
        return count;
    }

    public boolean isPrime(int n){
        for (int i = 2; i < n; i++) {
            if(n % i == 0){
                return false;
            }
        }

        return true;
    }

    public static void main(String[] args) {
        Easy204 easy204 = new Easy204();
        System.out.println(easy204.countPrimes(100));
    }
}
