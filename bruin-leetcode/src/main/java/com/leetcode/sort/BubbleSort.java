package com.leetcode.sort;

import com.leetcode.DataUtil;

/**
 * @description: 排序 O(n2)级别
 * @author: xiongwenwen
 * @date: 2020/9/24 16:20
 */
public class BubbleSort {

    /**
     * 冒泡排序
     */
    public static void bubbleSort(int[] nums){
        if(nums.length <= 1){
            return;
        }

        for (int i = 0; i < nums.length; i++) {
            boolean flag = false;
            for (int j = 0; j < nums.length - i - 1; j++) {
                if(nums[j] > nums[j + 1]){
                    int temp = nums[j];
                    nums[j] = nums[j + 1];
                    nums[j + 1] = temp;

                    flag = true;
                }
            }
            //没有数据交换，提前跳出循环
            if(!flag){
                break;
            }
        }
    }

    /**
     * 选择排序
     */
    public static void selectSort(int[] nums){
        if(nums.length <= 1){
            return;
        }
        for (int i = 0; i < nums.length - 1; i++) {
            int minIndex = i;
            for (int j = i + 1; j < nums.length; j++) {
                if(nums[minIndex] > nums[j]){
                    minIndex = j;
                }
            }
            int temp = nums[i];
            nums[i] = nums[minIndex];
            nums[minIndex] = temp;
        }
    }

    /**
     * 插入排序
     */
    public static void insertSort(int[] nums){
        for (int i = 1; i < nums.length; i++) {

            // 记录要插入的数据
            int tmp = nums[i];

            // 从已经排序的序列最右边的开始比较，找到比其小的数
            int j = i;
            while (j > 0 && tmp < nums[j - 1]) {
                nums[j] = nums[j - 1];
                j--;
            }

            // 存在比其小的数，插入
            nums[j] = tmp;

            DataUtil.printAll(nums);
        }
    }



    public static void main(String[] args) {
        int[] nums = {5,2,4,3,8,7,9,1,6};

        insertSort(nums);
        DataUtil.printAll(nums);
    }
}
