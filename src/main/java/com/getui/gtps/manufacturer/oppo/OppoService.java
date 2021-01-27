package com.getui.gtps.manufacturer.oppo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.getui.gtps.config.CommonConfig;
import com.getui.gtps.exception.AuthFailedException;
import com.getui.gtps.manufacturer.BaseManufacturer;
import com.getui.gtps.manufacturer.CacheServiceFactory;
import com.getui.gtps.manufacturer.CaffeineCacheService;
import com.getui.gtps.manufacturer.Result;
import com.getui.gtps.util.Encrypt;
import com.getui.gtps.util.FileUtils;
import com.getui.gtps.util.HttpResponse;
import com.getui.gtps.util.HttpUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import static com.getui.gtps.config.GtSDKConstants.CommandPreValue.BySha1;
import static com.getui.gtps.manufacturer.constant.ManufacturerConstants.MANUFACTURER_NAME_OPPO;

/**
 * oppo厂商的服务实现类
 *
 * @author wangxu
 * date: 2020/12/25
 * email：wangx2@getui.com
 */
public class OppoService extends BaseManufacturer {

    private static final Logger LOGGER = LoggerFactory.getLogger(OppoService.class);

    public final static String name = MANUFACTURER_NAME_OPPO;

    private final AtomicLong authLock = new AtomicLong(System.currentTimeMillis());

    public OppoService(String appId, String appKey, String appSecret, String masterSecret) {
        super(appId, appKey, appSecret, masterSecret);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    protected void auth() throws AuthFailedException {
        synchronized (authLock) {
            // 可能并发操作已鉴权
            if (!this.needAuth()) {
                return;
            }
            Exception e = null;
            int i = 0;
            do {
                try {
                    long timestamp = System.currentTimeMillis();
                    Map<String, String> parameters = new HashMap<>();
                    parameters.put(OppoConstants.PARAM_APP_KEY, this.getAppKey());
                    parameters.put(OppoConstants.PARAM_TIMESTAMP, String.valueOf(timestamp));
                    parameters.put(OppoConstants.PARAM_SIGN, getSign(this.getAppKey(), timestamp, this.getMasterSecret()));
                    HttpResponse result = HttpUtils.post(OppoConstants.baseUrl + OppoConstants.RequestPath.AUTH.getPath(), parameters, 500);
                    LOGGER.info("OppoService auth result: {}", result.toString());
                    if (result.success()) {
                        JsonNode jsonNode = new ObjectMapper().readTree(result.getContent());
                        if (OppoConstants.ReturnCode.Success.getCode() == jsonNode.get("code").intValue()) {
                            String authToken = jsonNode.get("data").get("auth_token").textValue();
                            // OPPO说明：鉴权令牌有效期是24小时，在有效期内多次申请均返回相同的令牌，令牌超过有效期后申请将返回新令牌。令牌过期失效后有10分钟过渡期，过渡期间新旧两个auth_token均可使用，超过过渡期后只有新令牌可使用。
                            // TTL说明：为了防止24小时临界点调用接口时，op可能返回旧的token被我们换成24小时，这里+5分钟进行缓存
                            this.cacheMap.put(AUTH_TOKEN, CacheServiceFactory.getCacheService(CaffeineCacheService.class).set(name, authToken, 24 * 3600 + 5 * 60));
                            authLock.compareAndSet(authLock.get(), System.currentTimeMillis());
                        }
                    }
                } catch (JsonProcessingException ex) {
                    e = ex;
                }
            } while (isEmptyToken(getAuthTokenFromCache()) && i++ < 2);
            if (isEmptyToken(getAuthTokenFromCache())) {
                throw new AuthFailedException(this.getName(), e);
            }
        }
    }

    private static String getSign(String appKey, long timestamp, String masterSecret) {
        String plaintext = String.format("%s%s%s", appKey, timestamp, masterSecret);
        return Encrypt.SHA256(plaintext);
    }

    @Override
    protected boolean needAuth() {
        return isEmptyToken(getAuthTokenFromCache()) || System.currentTimeMillis() - authLock.get() >= 1000;
    }

    @Override
    protected String getAuthToken() {
        String token = getAuthTokenFromCache();
        if (isEmptyToken(token)) {
            auth();
            token = getAuthTokenFromCache();
        }
        return token;
    }

    private String getAuthTokenFromCache() {
        Object cache = this.cacheMap.get(AUTH_TOKEN);
        String token = null;
        if (cache != null) {
            token = (String) CacheServiceFactory.getCacheService(CaffeineCacheService.class).get(cache, name);
        }
        return token;
    }

    private boolean isEmptyToken(String token) {
        return token == null || token.length() == 0;
    }

    @Override
    public Result uploadIcon(File file) throws AuthFailedException {
        String cacheKey = BySha1.equals(CommonConfig.sameFileJudgePattern) ? FileUtils.sha1(file) : file.getName();
        Optional<Result> cacheResult = getCacheResult(ICON_URL, cacheKey);
        return cacheResult.orElseGet(() -> {
            Result result = uploadIcon(file, cacheKey);
            if (Result.invalidAuthToken().getCode() == result.getCode()) {
                auth();
                result = uploadIcon(file, cacheKey);
            }
            return result;
        });
    }

    private Result uploadIcon(File file, String cacheKey) {
        Map<String, String> parameters = new HashMap<>();
        parameters.put("auth_token", getAuthToken());
        parameters.put("picture_ttl", String.valueOf(30 * 24 * 3600));
        HttpResponse httpResponse = HttpUtils.postFile(OppoConstants.UpUrl + OppoConstants.RequestPath.UPLOAD_ICON.getPath(), null, parameters, file, CommonConfig.callTimeout);
        LOGGER.info("OppoService uploadIcon httpResponse: {}", httpResponse.toString());
        if (httpResponse.success()) {
            try {
                JsonNode jsonNode = new ObjectMapper().readTree(httpResponse.getContent());
                int returnCode = jsonNode.get("code").intValue();
                if (OppoConstants.ReturnCode.Success.getCode() == returnCode) {
                    String url = jsonNode.get("data").get("small_picture_id").textValue();
                    this.cacheMap.put(ICON_URL, CacheServiceFactory.getCacheService(CaffeineCacheService.class).set(cacheKey, url));
                    return Result.success(url);
                } else if (OppoConstants.ReturnCode.InvalidAuthCode.getCode() == returnCode) {
                    return Result.invalidAuthToken();
                } else {
                    return Result.fail(jsonNode.get("message").textValue());
                }
            } catch (JsonProcessingException e) {
                LOGGER.error("OppoService uploadIcon error. ", e);
            }
        }
        return Result.fail(httpResponse.getMessage());
    }

    @Override
    public Result uploadPic(File file) {
        String cacheKey = BySha1.equals(CommonConfig.sameFileJudgePattern) ? FileUtils.sha1(file) : file.getName();
        Optional<Result> cacheResult = getCacheResult(PIC_URL, cacheKey);
        return cacheResult.orElseGet(() -> {
            Result result = uploadPic(file, cacheKey);
            if (Result.invalidAuthToken().getCode() == result.getCode()) {
                auth();
                result = uploadPic(file, cacheKey);
            }
            return result;
        });
    }

    private Result uploadPic(File file, String cacheKey) {
        Map<String, String> parameters = new HashMap<>();
        parameters.put("auth_token", getAuthToken());
        parameters.put("picture_ttl", String.valueOf(30 * 24 * 3600));
        HttpResponse httpResponse = HttpUtils.postFile(OppoConstants.UpUrl + OppoConstants.RequestPath.UPLOAD_PIC.getPath(), null, parameters, file, CommonConfig.callTimeout);
        LOGGER.info("OppoService uploadIcon httpResponse: {}", httpResponse.toString());
        if (httpResponse.success()) {
            try {
                JsonNode jsonNode = new ObjectMapper().readTree(httpResponse.getContent());
                int returnCode = jsonNode.get("code").intValue();
                if (OppoConstants.ReturnCode.Success.getCode() == returnCode) {
                    String url = jsonNode.get("data").get("big_picture_id").textValue();
                    this.cacheMap.put(PIC_URL, CacheServiceFactory.getCacheService(CaffeineCacheService.class).set(cacheKey, url));
                    return Result.success(url);
                } else if (OppoConstants.ReturnCode.InvalidAuthCode.getCode() == returnCode) {
                    return Result.invalidAuthToken();
                } else {
                    return Result.fail(jsonNode.get("message").textValue());
                }
            } catch (JsonProcessingException e) {
                LOGGER.error("OppoService uploadIcon error. ", e);
            }
        }
        return Result.fail(httpResponse.getMessage());
    }
}
