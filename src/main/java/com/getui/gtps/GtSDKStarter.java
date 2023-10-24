package com.getui.gtps;


import com.getui.gtps.config.CommonConfig;
import com.getui.gtps.manufacturer.ManufacturerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * date: 2020/12/25
 */
public class GtSDKStarter {

    private static final Logger LOGGER = LoggerFactory.getLogger(GtSDKStarter.class);

    private GtSDKStarter() {

    }

    private static class SingletonHolder {
        private final static GtSDKStarter instance = new GtSDKStarter();
    }

    public static GtSDKStarter getInstance() {
        return SingletonHolder.instance;
    }

    /**
     * 加载配置文件
     *
     * @param fileName 配置文件名，当前方法调用者类的ClassLoader所在路径下的配置文件
     * @return GtSDKStarter
     * @throws IOException 加载配置文件时可能会有IOException
     */
    public GtSDKStarter loadPropertyFile(String fileName) throws ClassNotFoundException, IOException {
        InputStream inputStream = Class.forName(Thread.currentThread().getStackTrace()[2].getClassName()).getClassLoader().getResourceAsStream(fileName);
        if (inputStream != null) {
            CommonConfig.manufacturerProperties.load(Class.forName(Thread.currentThread().getStackTrace()[2].getClassName()).getClassLoader().getResourceAsStream(fileName));
            return this;
        }
        throw new FileNotFoundException(fileName);
    }

    /**
     * sdk初始化，包括配置初始化和厂商实例初始化
     */
    public void init() {
        LOGGER.info("GT SDKInit start.");
        // 从启动参数或配置文件中进行参数初始化
        CommonConfig.init();
        // 如果参数配置开启了厂商初始就进行厂商初始化：包括厂商服务类实例化和鉴权
        if (CommonConfig.manufacturerInitSwitch) {
            ManufacturerFactory.init();
        }
        LOGGER.info("GT SDKInit finish.");
    }

}
