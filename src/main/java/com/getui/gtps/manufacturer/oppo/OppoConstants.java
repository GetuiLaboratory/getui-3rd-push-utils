package com.getui.gtps.manufacturer.oppo;

/**
 * date: 2020/12/27
 */
public class OppoConstants {
    static final String PARAM_APP_KEY = "app_key";
    static final String PARAM_SIGN = "sign";
    static final String PARAM_TIMESTAMP = "timestamp";

    static String baseUrl = "https://api.push.oppomobile.com/server/v1";
    static String UpUrl = "https://api-media.push.heytapmobi.com/server/v1";

    enum RequestPath {
        AUTH("/auth"),
        UPLOAD_ICON("/media/upload/small_picture"),
        UPLOAD_PIC("/media/upload/big_picture");

        private final String path;

        RequestPath(String path) {
            this.path = path;
        }

        String getPath() {
            return this.path;
        }
    }

    enum ReturnCode {
        Success(0),
        InvalidAuthCode(11);

        private final int code;

        ReturnCode(int code) {
            this.code = code;
        }

        int getCode() {
            return this.code;
        }
    }
}
