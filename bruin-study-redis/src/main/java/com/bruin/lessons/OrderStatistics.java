package com.bruin.lessons;

import com.bruin.utils.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

/**
 * @description: 排序统计
 * @author: xiongwenwen
 * @date: 2020/10/18 15:53
 */
@Service
public class OrderStatistics {

    @Autowired
    private RedisUtil redisUtil;

    public void orderStatisticsByList(){
        redisUtil.delete("discuss");
        redisUtil.lRightPushAll("discuss", Arrays.asList("a","b","c","d","e","f"));

        List<String> discuss = redisUtil.lRange("discuss",3, 5);
        System.out.println(Arrays.toString(discuss.toArray()));
        redisUtil.lLeftPush("discuss", "g");
        List<String> discuss1 = redisUtil.lRange("discuss",3, 5);
        System.out.println(Arrays.toString(discuss1.toArray()));

    }

    public void orderStatisticsBySortedSet(){
        redisUtil.delete("discuss");
        List<String> discuses = Arrays.asList("a","b","c","d","e","f");
        for (int i = 0; i < discuses.size(); i++) {
            redisUtil.zAdd("discuss", discuses.get(i), i);
        }

        List<String> discuss = redisUtil.lRange("discuss",3, 5);
        System.out.println(Arrays.toString(discuss.toArray()));
        redisUtil.zAdd("discuss", "g", 6);
        List<String> discuss1 = redisUtil.lRange("discuss",3, 5);
        System.out.println(Arrays.toString(discuss1.toArray()));

    }
}
