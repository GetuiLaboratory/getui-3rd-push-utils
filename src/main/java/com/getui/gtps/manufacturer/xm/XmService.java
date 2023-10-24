package com.getui.gtps.manufacturer.xm;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.getui.gtps.config.CommonConfig;
import com.getui.gtps.manufacturer.BaseManufacturer;
import com.getui.gtps.manufacturer.CacheServiceFactory;
import com.getui.gtps.manufacturer.CaffeineCacheService;
import com.getui.gtps.manufacturer.Result;
import com.getui.gtps.manufacturer.oppo.OppoService;
import com.getui.gtps.util.FileUtils;
import com.getui.gtps.util.HttpResponse;
import com.getui.gtps.util.HttpUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.getui.gtps.config.GtSDKConstants.CommandPreValue.BySha1;
import static com.getui.gtps.manufacturer.constant.ManufacturerConstants.MANUFACTURER_NAME_XM;

/**
 * xm厂商的服务实现类
 *
 * date: 2020/12/25
 */
public class XmService extends BaseManufacturer {

    private static final Logger LOGGER = LoggerFactory.getLogger(XmService.class);

    public final static String name = MANUFACTURER_NAME_XM;

    public XmService(String appId, String appKey, String appSecret, String masterSecret) {
        super(appId, appKey, appSecret, masterSecret);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    protected void auth() {
        LOGGER.info("XmService do not need auth.");
    }

    @Override
    protected String getAuthToken() {
        return null;
    }

    @Override
    public boolean needAuth() {
        return false;
    }

    @Override
    public Result uploadIcon(File file) {
        String cacheKey = BySha1.equals(CommonConfig.sameFileJudgePattern) ? FileUtils.sha1(file) : file.getName();
        Optional<Result> cacheResult = getCacheResult(ICON_URL, cacheKey);
        return cacheResult.orElseGet(() -> uploadIcon(file, cacheKey));
    }

    private Result uploadIcon(File file, String cacheKey) {
        Map<String, String> headParameters = new HashMap<>();
        headParameters.put("Authorization", "key=" + this.getAppSecret());
        Map<String, String> requestParameters = new HashMap<>();
        requestParameters.put("is_global", "false");
        requestParameters.put("is_icon", "true");
        HttpResponse httpResponse = HttpUtils.postFile(XmConstants.RequestPath.UPLOAD_PIC.getPath(), headParameters, requestParameters, file, CommonConfig.callTimeout);
        LOGGER.info("XmService uploadIcon httpResponse: {}", httpResponse.toString());
        if (httpResponse.success()) {
            try {
                JsonNode jsonNode = new ObjectMapper().readTree(httpResponse.getContent());
                if (0 == jsonNode.get("code").intValue()) {
                    String url = jsonNode.get("data").get("icon_url").textValue();
                    this.cacheMap.put(ICON_URL, CacheServiceFactory.getCacheService(CaffeineCacheService.class).set(cacheKey, url));
                    return Result.success(url);
                } else {
                    return Result.fail(jsonNode.toString());
                }
            } catch (JsonProcessingException e) {
                LOGGER.error("XmService uploadIcon error. ", e);
            }
        }
        return Result.fail(httpResponse.getMessage());
    }

    @Override
    public Result uploadPic(File file) {
        String cacheKey = BySha1.equals(CommonConfig.sameFileJudgePattern) ? FileUtils.sha1(file) : file.getName();
        Optional<Result> cacheResult = getCacheResult(PIC_URL, cacheKey);
        return cacheResult.orElseGet(() -> uploadPic(file, cacheKey));
    }

    private Result uploadPic(File file, String cacheKey) {
        Map<String, String> headParameters = new HashMap<>();
        headParameters.put("Authorization", "key=" + this.getAppSecret());
        Map<String, String> requestParameters = new HashMap<>();
        requestParameters.put("is_global", "false");
        requestParameters.put("is_icon", "false");
        HttpResponse httpResponse = HttpUtils.postFile(XmConstants.RequestPath.UPLOAD_PIC.getPath(), headParameters, requestParameters, file, CommonConfig.callTimeout);
        LOGGER.info("XmService uploadPic httpResponse: {}", httpResponse.toString());
        if (httpResponse.success()) {
            try {
                JsonNode jsonNode = new ObjectMapper().readTree(httpResponse.getContent());
                if (0 == jsonNode.get("code").intValue()) {
                    String url = jsonNode.get("data").get("pic_url").textValue();
                    this.cacheMap.put(PIC_URL, CacheServiceFactory.getCacheService(CaffeineCacheService.class).set(cacheKey, url));
                    return Result.success(url);
                } else {
                    return Result.fail(jsonNode.toString());
                }
            } catch (JsonProcessingException e) {
                LOGGER.error("XmService uploadIcon error. ", e);
            }
        }
        return Result.fail(httpResponse.getMessage());
    }
}
