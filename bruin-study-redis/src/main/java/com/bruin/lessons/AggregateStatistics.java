package com.bruin.lessons;

import com.bruin.utils.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @description: 聚合统计
 * @author: xiongwenwen
 * @date: 2020/10/18 10:49
 */
@Service
public class AggregateStatistics {

    @Autowired
    private RedisUtil redisUtil;

    public void statisticsLoginUser(){
        userLoginCache("20201016", new int[]{1001,1002,1003});
        redisUtil.sUnionAndStore("user:login", "user:login:20201016", "user:login");

        userLoginCache("20201017", new int[]{1002,1006,1007});

        //并集：累计登陆用户
//        redisUtil.sUnionAndStore("user:login", "user:login:20201017", "user:login");
//        System.out.println(redisUtil.sSize("user:login"));

        //差集：新增登陆用户
        redisUtil.sDifference("user:login", "user:login:20201017", "user:login:new");
        System.out.println(redisUtil.sSize("user:login:new"));

        //交集：留存用户数
        redisUtil.sIntersectAndStore("user:login", "user:login:20201017", "user:login:again");
        System.out.println(redisUtil.sSize("user:login:again"));
    }

    /**
     * 每天登陆的用户
     * @param day
     * @param userIds
     */
    private void userLoginCache(String day, int[] userIds){
        for (int id: userIds) {
            redisUtil.sAdd("user:login", id);
            redisUtil.sAdd("user:login:" + day, id);
        }
    }
}
