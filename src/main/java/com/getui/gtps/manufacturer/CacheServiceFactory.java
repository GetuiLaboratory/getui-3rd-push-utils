package com.getui.gtps.manufacturer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * date: 2020/12/28
 */
public class CacheServiceFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(CacheServiceFactory.class);

    private static final Map<String, Object> locks = new ConcurrentHashMap<>();

    private static final Map<String, CacheService> factory = new HashMap<>();

    public static CacheService getCacheService(Class<? extends CacheService> clazz) {
        String className = clazz.getName();
        if (factory.get(className) == null) {
            synchronized (locks.computeIfAbsent(className, k -> new Object())) {
                if (factory.get(className) == null) {
                    try {
                        Class<?> cClass = Class.forName(className);
                        if (CacheService.class.isAssignableFrom(cClass)) {
                            factory.put(className, (CacheService) cClass.newInstance());
                        }
                    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
                        LOGGER.error("GT SDK getCacheService fail. ", e);
                    }
                }
            }
        }
        return factory.get(className);
    }
}
