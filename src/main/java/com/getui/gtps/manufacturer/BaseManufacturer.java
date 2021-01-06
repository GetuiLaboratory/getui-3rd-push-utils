package com.getui.gtps.manufacturer;

import com.getui.gtps.exception.AuthFailedException;
import com.getui.gtps.manufacturer.service.ManufacturerService;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 厂商抽象类，定义了多厂商共有的特效
 *
 * @author wangxu
 * date: 2020/12/25
 * email：wangx2@getui.com
 */
public abstract class BaseManufacturer implements ManufacturerService {

    protected final static String AUTH_TOKEN = "AuthToken";

    protected final static String ICON_URL = "IconUrl";

    protected final static String PIC_URL = "PicUrl";

    private final String appId;

    private final String appKey;

    private final String appSecret;

    private final String masterSecret;

    /**
     * 为了提高接口性能，使用缓存存储一些接口调用结果，避免http接口重复调用
     */
    protected Map<String, Object> cacheMap;

    protected BaseManufacturer(String appId, String appKey, String appSecret, String masterSecret) {
        this.appId = appId;
        this.appKey = appKey;
        this.appSecret = appSecret;
        this.masterSecret = masterSecret;
        this.cacheMap = new ConcurrentHashMap<>();
    }

    /**
     * @return 厂商的名称
     */
    public abstract String getName();

    /**
     * @return 该厂商是否需要鉴权
     */
    protected abstract boolean needAuth();

    /**
     * 厂商服务鉴权
     */
    protected abstract void auth() throws AuthFailedException;

    /**
     * @return 获取该厂商的authToken，不需要鉴权的厂商不需要调用
     */
    protected abstract String getAuthToken();

    protected String getAppId() {
        return appId;
    }

    protected String getAppKey() {
        return appKey;
    }

    protected String getAppSecret() {
        return appSecret;
    }

    protected String getMasterSecret() {
        return masterSecret;
    }

    protected Optional<Result> getCacheResult(String mapKey, String cacheKey) {
        Object cache = CacheServiceFactory.getCacheService(CaffeineCacheService.class).get(this.cacheMap.get(mapKey), cacheKey);
        return cache != null && !"".equals(cache) ? Optional.of(Result.success((String) cache)) : Optional.empty();
    }

}
