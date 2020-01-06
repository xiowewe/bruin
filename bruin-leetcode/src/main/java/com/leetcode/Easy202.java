package com.leetcode;

/**
 * @description: isHappy
 * @author: xiongwenwen   2020/1/2 17:04
 */
public class Easy202 {
    public boolean isHappy(int n) {
        //快慢指针判断重复
        int slow = n, fast = n;

        do{
            slow = caculate(slow);
            fast = caculate(fast);
            fast = caculate(fast);
        }while(slow != fast);

        return slow == 1;
    }

    public int caculate(int n){
        int sum = 0;
        while(n > 0){
            int m = n % 10;
            sum += m * m;
            n = n / 10;
        }
        return sum;
    }
}
