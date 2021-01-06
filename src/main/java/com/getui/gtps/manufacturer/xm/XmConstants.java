package com.getui.gtps.manufacturer.xm;

/**
 * @author wangxu
 * date: 2020/12/31
 * email：wangx2@getui.com
 */
public class XmConstants {
    static String baseUrl = "https://api.xmpush.xiaomi.com";

    enum RequestPath {
        UPLOAD_ICON("/media/upload/smallIcon"),
        UPLOAD_PIC("/media/upload/image");

        private final String path;

        RequestPath(String path) {
            this.path = path;
        }

        String getPath() {
            return baseUrl + this.path;
        }
    }
}
