package com.bruin.utils;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.exceptions.JedisConnectionException;

import java.util.ArrayList;
import java.util.List;

/**
 * @description: 1、通过命令生成多个节点的sha校验码: ./redis-cli -p 6380 script load "$(cat redis-script-node1.lua)"
 * @author: xiongwenwen   2019/12/9 10:34
 */
public class RedisIDGenerator {
    static final Logger logger = LoggerFactory.getLogger(RedisIDGenerator.class);

    /**
     * JedisPool, luaSha
     */
    List<Pair<JedisPool, String>> jedisPoolList;
    int retryTimes;

    int index = 0;

    private RedisIDGenerator() {

    }

    private RedisIDGenerator(List<Pair<JedisPool, String>> jedisPoolList,
                        int retryTimes) {
        this.jedisPoolList = jedisPoolList;
        this.retryTimes = retryTimes;
    }

    static public RedisIDGeneratorBuilder builder() {
        return new RedisIDGeneratorBuilder();
    }

    static class RedisIDGeneratorBuilder {
        List<Pair<JedisPool, String>> jedisPoolList = new ArrayList();
        int retryTimes = 5;

        /**
         * @param host  host
         * @param port  端口
         * @param luaSha    sha校验吗
         * @return
         */
        public RedisIDGeneratorBuilder addHost(String host, int port, String luaSha) {
            jedisPoolList.add(Pair.of(new JedisPool(host, port), luaSha));
            return this;
        }

        public RedisIDGeneratorBuilder retryTimes(int retryTimes) {
            this.retryTimes = retryTimes;
            return this;
        }

        public RedisIDGenerator build() {
            return new RedisIDGenerator(jedisPoolList, retryTimes);
        }
    }

    public long next(String tab) {
        return next(tab, 0);
    }

    public long next(String tab, long shardId) {
        for (int i = 0; i < retryTimes; ++i) {
            Long id = innerNext(tab, shardId);
            if (id != null) {
                return id;
            }
        }
        throw new RuntimeException("Can not generate id!");
    }

    Long innerNext(String tab, long shardId) {
        index++;
        Pair<JedisPool, String> pair = jedisPoolList.get(index
                % jedisPoolList.size());
        JedisPool jedisPool = pair.getLeft();

        String luaSha = pair.getRight();
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            List<Long> result = (List<Long>) jedis.evalsha(luaSha, 2, tab, "" + shardId);
            long id = buildId(result.get(0), result.get(1), result.get(2), result.get(3));

            return id;
        } catch (JedisConnectionException e) {
            if (jedis != null) {
                jedisPool.returnBrokenResource(jedis);
            }
            logger.error("generate id error!", e);
        } finally {
            if (jedis != null) {
                jedisPool.returnResource(jedis);
            }
        }
        return null;
    }

    public static long buildId(long second, long microSecond, long shardId,
                               long seq) {
        long miliSecond = (second * 1000 + microSecond / 1000);
        return (miliSecond << (12 + 10)) + (shardId << 10) + seq;
    }

    public static List<Long> parseId(long id) {
        long miliSecond = id >>> 22;
        // 2 ^ 12 = 0xFFF
        long shardId = (id & (0xFFF << 10)) >> 10;
        long seq = id & 0x3FF;

        List<Long> re = new ArrayList<Long>(4);
        re.add(miliSecond);
        re.add(shardId);
        re.add(seq);
        return re;
    }
}
