package com.getui.gtps.config;

/**
 * sdk常量
 *
 * @author wangxu
 * date: 2020/12/25
 * email：wangx2@getui.com
 */
public class GtSDKConstants {

    public static class Prefix {
        public final static String GtSDK = "GtSDK.";
    }

    public static class Suffix {
        public final static String AppId = ".AppId";
        public final static String AppKey = ".AppKey";
        public final static String AppSecret = ".AppSecret";
        public final static String AppMasterSecret = ".MasterSecret";
    }

    public static class FilePropertyName {
        public final static String ManufacturerInitSwitch = Prefix.GtSDK + "manufacturerInitSwitch";
        public final static String ModuleSet = Prefix.GtSDK + "moduleSet";
        public final static String JudgeFile = Prefix.GtSDK + "judgeFile";
        public final static String CallTimeout = Prefix.GtSDK + "callTimeout";
    }

    public static class CommandPreValue {

        public final static String AllModule = "All";

        public final static String ByName = "fileName";
        public final static String BySha1 = "sha1";
    }


}
