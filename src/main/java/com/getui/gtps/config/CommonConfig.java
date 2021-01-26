package com.getui.gtps.config;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.getui.gtps.config.GtSDKConstants.CommandPreValue.*;
import static com.getui.gtps.config.GtSDKConstants.FilePropertyName.*;

/**
 * sdk配置参数 <br>
 * 参数都是public的，用户可以按照sdk初始化流程进行初始化，也可以自己在厂商实例初始化前自定义指定参数值
 *
 * @author wangxu
 * date: 2020/12/25
 */
public class CommonConfig {

    /* 以下是sdk初始化使用参数以及默认值 */

    /**
     * 初始化开关，默认打开
     */
    public static boolean manufacturerInitSwitch = true;

    /**
     * SDK接口的执行厂商范围，默认全部。<br>
     * 例如：用户只配置了MZ，那么初始化的时候只获取魅族的accessToken，上传icon的时候只上传到魅族，即使SDK里也实现了其他厂商的获取accessToken和上传icon的代码也不执行。
     */
    public static Set<String> moduleSet = Stream.of(AllModule).collect(Collectors.toCollection(HashSet::new));

    /**
     * 判断文件是否相同的方式。默认按照文件名判断，可选按照文件sha1值判断
     */
    public static String sameFileJudgePattern = ByName;

    /**
     * 多厂商接口调用默认是否多线程
     */
    public static boolean mThread = true;

    /**
     * 接口调用超时等待时间，单位毫秒，默认500毫秒
     */
    public static int callTimeout = 500;

    /* 以下是厂商参数 */

    /**
     * 厂商参数
     */
    public static Properties manufacturerProperties = new Properties();

    /* 以下是sdk参数的初始化 */

    /**
     * 以下是sdk参数的初始化
     */
    public static void init() {
        initPropertiesByFile();
    }

    /**
     * 按照配置文件进行参数初始化
     */
    private static void initPropertiesByFile() {
        CommonConfig.manufacturerInitSwitch = !Objects.equals(false, Boolean.parseBoolean(manufacturerProperties.getProperty(ManufacturerInitSwitch, "true")));
        String moduleString = manufacturerProperties.getProperty(ModuleSet);
        if (moduleString != null) {
            String[] modules = moduleString.split(",");
            CommonConfig.moduleSet.remove(AllModule);
            Collections.addAll(CommonConfig.moduleSet, modules);
        }
        CommonConfig.sameFileJudgePattern = BySha1.equalsIgnoreCase(manufacturerProperties.getProperty(JudgeFile)) ? BySha1 : ByName;
        CommonConfig.mThread = !Objects.equals(false, Boolean.parseBoolean(manufacturerProperties.getProperty(MThread, "true")));
        CommonConfig.callTimeout = Integer.parseInt((String) manufacturerProperties.getOrDefault(CallTimeout, String.valueOf(CommonConfig.callTimeout)));
    }

}
