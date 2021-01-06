package com.getui.gtps.manufacturer;

/**
 * 缓存服务，用于sdk中需要使用缓存的地方，可用于自定义的缓存方式的实现。 <br>
 * 现在默认的缓存方式是CaffeineCache，待有需求时再迭代缓存方式切换的代码
 *
 * @author wangxu
 * date: 2020/12/28
 * email：wangx2@getui.com
 */
public interface CacheService {

    Object set(String key, Object value);

    Object set(String key, Object value, int expire);

    Object get(Object cache, String key);

}
