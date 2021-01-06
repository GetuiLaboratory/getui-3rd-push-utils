package com.getui.gtps.manufacturer;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import java.util.concurrent.TimeUnit;

/**
 * CaffeineCache缓存服务实现
 *
 * @author wangxu
 * date: 2020/12/28
 * email：wangx2@getui.com
 */
public class CaffeineCacheService implements CacheService {

    @Override
    public Object set(String key, Object value) {
        Cache<String, Object> cache = Caffeine.newBuilder()
                .maximumSize(1)
                .build();
        cache.put(key, value);
        return cache;
    }

    @Override
    public Object set(String key, Object value, int expire) {
        Cache<String, Object> cache = Caffeine.newBuilder()
                .maximumSize(1)
                .expireAfterWrite(expire, TimeUnit.SECONDS)
                .build();
        cache.put(key, value);
        return cache;
    }

    @Override
    public Object get(Object cache, String key) {
        return cache instanceof Cache ? ((Cache) cache).getIfPresent(key) : null;
    }
}
