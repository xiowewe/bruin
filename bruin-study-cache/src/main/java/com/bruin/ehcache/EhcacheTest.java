package com.bruin.ehcache;

import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.config.units.EntryUnit;
import org.ehcache.expiry.ExpiryPolicy;

/**
 * @description:
 * @author: xiongwenwen   2019/11/21 17:19
 */
public class EhcacheTest {
    public static void main(String[] args) {
        CacheManager cacheManager = CacheManagerBuilder.newCacheManagerBuilder()
                .withCache("preConfigured",
                        CacheConfigurationBuilder.newCacheConfigurationBuilder(Long.class, String.class,
                                ResourcePoolsBuilder.heap(100))
                                .build())
                .build(true);

        Cache<Long, String> preConfigured
                = cacheManager.getCache("preConfigured", Long.class, String.class);

        Cache<Long, String> myCache = cacheManager.createCache("myCache",
                CacheConfigurationBuilder.newCacheConfigurationBuilder(Long.class, String.class,
                        ResourcePoolsBuilder.newResourcePoolsBuilder()
                                .heap(100, EntryUnit.ENTRIES))
                        .withDispatcherConcurrency(4).withExpiry(ExpiryPolicy.NO_EXPIRY)
                        /*.withSizeOfMaxObjectGraph(3)
                        .withSizeOfMaxObjectSize(1, MemoryUnit.KB)*/);

        myCache.put(1L, "da one!");
        String value = myCache.get(1L);

        cacheManager.close();

    }
}
