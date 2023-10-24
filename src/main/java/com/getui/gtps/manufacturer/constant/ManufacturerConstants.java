package com.getui.gtps.manufacturer.constant;

/**
 * 厂商服务常量
 *
 * date: 2020/12/25
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
