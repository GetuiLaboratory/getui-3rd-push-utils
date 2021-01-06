package com.getui.gtps.manufacturer.constant;

/**
 * 厂商服务常量
 *
 * @author wangxu
 * date: 2020/12/25
 * email：wangx2@getui.com
 */
public class ManufacturerConstants {

    public final static String MANUFACTURER_NAME_XM = "XM";
    public final static String MANUFACTURER_NAME_OPPO = "OPPO";

    public enum ManufacturerName {

        XM(MANUFACTURER_NAME_XM),
        OPPO(MANUFACTURER_NAME_OPPO);

        private String name;

        ManufacturerName(String name) {
            this.name = name;
        }

        public String getName() {
            return this.name;
        }
    }


}
