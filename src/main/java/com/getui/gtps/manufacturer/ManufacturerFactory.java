package com.getui.gtps.manufacturer;

import com.getui.gtps.config.CommonConfig;
import com.getui.gtps.config.GtSDKConstants;
import com.getui.gtps.exception.AuthFailedException;
import com.getui.gtps.manufacturer.oppo.OppoService;
import com.getui.gtps.manufacturer.xm.XmService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.getui.gtps.config.GtSDKConstants.CommandPreValue.AllModule;

/**
 * 厂商工厂：包含多厂商服务实例初始化、鉴权以及接口服务
 *
 * @author wangxu
 * date: 2020/12/25
 * email：wangx2@getui.com
 */
public class ManufacturerFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(ManufacturerFactory.class);

    private static final Set<Class<? extends BaseManufacturer>> subTypes = new HashSet<Class<? extends BaseManufacturer>>() {
        {
            add(XmService.class);
            add(OppoService.class);
        }
    };

    /**
     * 手动配置线程执行器的线程池大小，线程数请结合BaseManufacturer子类梳理调节
     */
    private final static Executor myExecutor = Executors.newFixedThreadPool(2, r -> {
        Thread t = new Thread(r);
        // 使用守护线程保证不会阻止程序的关停
        t.setDaemon(true);
        return t;
    });

    private static boolean init;

    private static final Map<String, BaseManufacturer> factory = new HashMap<>();

    /**
     * 多厂商服务实例初始化和鉴权
     */
    public static synchronized void init() {
        if (!init) {
            initManufacturersInstance();
            initAuth();
            init = true;
        }
    }

    /**
     * 多厂商服务实例初始化
     */
    private static void initManufacturersInstance() {
        subTypes.forEach(clazz -> {
            String name = "";
            try {
                Field f = clazz.getDeclaredField("name");
                f.setAccessible(true);
                name = f.get(null).toString();
            } catch (NoSuchFieldException | IllegalAccessException e) {
                LOGGER.error("GT SDK initManufacturersInstance error. ", e);
            }
            if (needInstantiation(name) && !factory.containsKey(name)) {
                try {
                    Constructor<? extends BaseManufacturer> constructor = clazz.getConstructor(String.class, String.class, String.class, String.class);
                    String appId = CommonConfig.manufacturerProperties.getProperty(GtSDKConstants.Prefix.GtSDK + name + GtSDKConstants.Suffix.AppId);
                    String appKey = CommonConfig.manufacturerProperties.getProperty(GtSDKConstants.Prefix.GtSDK + name + GtSDKConstants.Suffix.AppKey);
                    String appSecret = CommonConfig.manufacturerProperties.getProperty(GtSDKConstants.Prefix.GtSDK + name + GtSDKConstants.Suffix.AppSecret);
                    String masterSecret = CommonConfig.manufacturerProperties.getProperty(GtSDKConstants.Prefix.GtSDK + name + GtSDKConstants.Suffix.AppMasterSecret);
                    BaseManufacturer instance = constructor.newInstance(appId, appKey, appSecret, masterSecret);
                    factory.put(name, instance);
                } catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
                    LOGGER.error("GT SDK initManufacturersInstance error. ", e);
                }
            }
        });
    }

    private static boolean needInstantiation(String name) {
        return CommonConfig.moduleSet.contains(AllModule) || CommonConfig.moduleSet.stream().anyMatch(name::equalsIgnoreCase);
    }

    /**
     * 多厂商服务鉴权
     */
    private static void initAuth() {
        if (factory.size() > 0) {
            factory.values().parallelStream().forEach(e -> {
                try {
                    e.auth();
                } catch (AuthFailedException ex) {
                    LOGGER.error("GT SDK initAuth error. ", ex);
                }
            });
        }
    }

    /**
     * 多厂商icon上传，多厂商上传同一个icon文件，多线程执行
     *
     * @param file 本地icon文件
     * @return 多厂商icon上传结果
     * @throws FileNotFoundException 本地icon文件找不到
     */
    public static Map<String, Result> uploadIcon(File file) throws FileNotFoundException {
        return uploadIcon(CommonConfig.mThread, file);
    }

    /**
     * 多厂商icon上传，多厂商上传同一个icon文件
     *
     * @param mThread 是否使用多线程
     * @param file    本地icon文件
     * @return 多厂商icon上传结果
     * @throws FileNotFoundException 本地icon文件找不到
     */
    public static Map<String, Result> uploadIcon(boolean mThread, File file) throws FileNotFoundException {
        if (!file.exists()) {
            throw new FileNotFoundException(file.getAbsolutePath());
        }
        init();
        Map<String, Result> result = new HashMap<>(factory.size());
        if (factory.size() > 0) {
            if (mThread) {
                Map<String, CompletableFuture<Result>> futures = factory.entrySet().stream()
                        .collect(Collectors.toMap(
                                Map.Entry::getKey,
                                e -> CompletableFuture.supplyAsync(() -> e.getValue().uploadIcon(file), myExecutor)));

                getFutureResult(result, futures, Thread.currentThread().getStackTrace()[1].getMethodName());
            } else {
                factory.forEach((k, v) -> {
                    try {
                        result.put(k, v.uploadIcon(file));
                    } catch (AuthFailedException e) {
                        result.put(k, Result.authFail());
                    }
                });
            }
        }
        return result;
    }

    /**
     * 多厂商icon上传，指定每个厂商上传的icon文件，多线程执行
     *
     * @param manufacturerFile icon文件
     * @return 多厂商icon上传结果
     */
    public static Map<String, Result> uploadIcon(ManufacturerFile... manufacturerFile) {
        return uploadIcon(CommonConfig.mThread, manufacturerFile);
    }

    /**
     * 多厂商icon上传，指定每个厂商上传的icon文件
     *
     * @param mThread          是否使用多线程
     * @param manufacturerFile icon文件
     * @return 多厂商icon上传结果
     */
    public static Map<String, Result> uploadIcon(boolean mThread, ManufacturerFile... manufacturerFile) {
        init();
        Map<String, Result> result = new HashMap<>(manufacturerFile.length);
        if (factory.size() > 0) {
            if (mThread) {
                Function<ManufacturerFile, CompletableFuture<Result>> valueMapper = file -> {
                    if (!file.exists()) {
                        return CompletableFuture.supplyAsync(() -> Result.fail(String.format("file %s not found", file.getAbsolutePath())), myExecutor);
                    }
                    Optional<BaseManufacturer> optional = Optional.ofNullable(factory.get(file.getManufacturerName()));
                    return optional.map(baseManufacturer -> CompletableFuture.supplyAsync(() -> baseManufacturer.uploadIcon(file), myExecutor))
                            .orElseGet(() -> CompletableFuture.supplyAsync(Result::noInstance, myExecutor));
                };

                Map<String, CompletableFuture<Result>> futures = Arrays.stream(manufacturerFile)
                        .collect(Collectors.toMap(ManufacturerFile::getManufacturerName, valueMapper));

                getFutureResult(result, futures, Thread.currentThread().getStackTrace()[1].getMethodName());
            } else {
                Arrays.stream(manufacturerFile).forEach(file -> {
                    BaseManufacturer manufacturer = factory.get(file.getManufacturerName());
                    try {
                        result.put(file.getManufacturerName(), manufacturer.uploadIcon(file));
                    } catch (AuthFailedException e) {
                        result.put(file.getManufacturerName(), Result.authFail());
                    }
                });
            }
        }
        return result;
    }

    /**
     * 多厂商图片上传，多厂商上传同一个图片文件，多线程执行
     *
     * @param file 本地图片文件
     * @return 多厂商图片上传结果
     * @throws FileNotFoundException 本地图片文件找不到
     */
    public static Map<String, Result> uploadPic(File file) throws FileNotFoundException {
        return uploadPic(CommonConfig.mThread, file);
    }

    /**
     * 多厂商图片上传，多厂商上传同一个图片文件
     *
     * @param mThread 是否使用多线程
     * @param file    本地图片文件
     * @return 多厂商图片上传结果
     * @throws FileNotFoundException 本地图片文件找不到
     */
    public static Map<String, Result> uploadPic(boolean mThread, File file) throws FileNotFoundException {
        if (!file.exists()) {
            throw new FileNotFoundException(file.getAbsolutePath());
        }
        init();
        Map<String, Result> result = new HashMap<>(factory.size());
        if (factory.size() > 0) {
            if (mThread) {
                Map<String, CompletableFuture<Result>> futures = factory.entrySet().stream()
                        .collect(Collectors.toMap(
                                Map.Entry::getKey,
                                e -> CompletableFuture.supplyAsync(() -> e.getValue().uploadPic(file), myExecutor)));

                getFutureResult(result, futures, Thread.currentThread().getStackTrace()[1].getMethodName());
            } else {
                factory.forEach((k, v) -> {
                    try {
                        result.put(k, v.uploadPic(file));
                    } catch (AuthFailedException e) {
                        result.put(k, Result.authFail());
                    }
                });
            }
        }
        return result;
    }

    /**
     * 多厂商图片上传，指定每个厂商上传的图片文件，多线程执行
     *
     * @param manufacturerFile 图片文件
     * @return 多厂商图片上传结果
     */
    public static Map<String, Result> uploadPic(ManufacturerFile... manufacturerFile) {
        return uploadPic(CommonConfig.mThread, manufacturerFile);
    }

    /**
     * 多厂商图片上传，指定每个厂商上传的图片文件
     *
     * @param mThread          是否使用多线程
     * @param manufacturerFile 图片文件
     * @return 多厂商图片上传结果
     */
    public static Map<String, Result> uploadPic(boolean mThread, ManufacturerFile... manufacturerFile) {
        init();
        Map<String, Result> result = new HashMap<>(manufacturerFile.length);
        if (factory.size() > 0) {
            if (mThread) {
                Function<ManufacturerFile, CompletableFuture<Result>> valueMapper = file -> {
                    if (!file.exists()) {
                        return CompletableFuture.supplyAsync(() -> Result.fail(String.format("file %s not found", file.getAbsolutePath())), myExecutor);
                    }
                    Optional<BaseManufacturer> optional = Optional.ofNullable(factory.get(file.getManufacturerName()));
                    return optional.map(baseManufacturer -> CompletableFuture.supplyAsync(() -> baseManufacturer.uploadPic(file), myExecutor))
                            .orElseGet(() -> CompletableFuture.supplyAsync(Result::noInstance, myExecutor));
                };

                Map<String, CompletableFuture<Result>> futures = Arrays.stream(manufacturerFile)
                        .collect(Collectors.toMap(ManufacturerFile::getManufacturerName, valueMapper));

                getFutureResult(result, futures, Thread.currentThread().getStackTrace()[1].getMethodName());
            } else {
                Arrays.stream(manufacturerFile).forEach(file -> {
                    BaseManufacturer manufacturer = factory.get(file.getManufacturerName());
                    try {
                        result.put(file.getManufacturerName(), manufacturer.uploadPic(file));
                    } catch (AuthFailedException e) {
                        result.put(file.getManufacturerName(), Result.authFail());
                    }
                });
            }
        }
        return result;
    }

    private static void getFutureResult(Map<String, Result> result, Map<String, CompletableFuture<Result>> futures, String function) {
        futures.forEach((k, v) -> {
            try {
                result.put(k, v.get(CommonConfig.callTimeout, TimeUnit.MILLISECONDS));
            } catch (TimeoutException e) {
                result.put(k, Result.timeout());
            } catch (ExecutionException | InterruptedException e) {
                if (e instanceof ExecutionException && e.getCause() instanceof AuthFailedException) {
                    result.put(k, Result.authFail());
                } else {
                    LOGGER.error("GT SDK {} {} fail. ", k, function, e);
                }
            }
        });
    }
}
