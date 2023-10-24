package com.getui.gtps.manufacturer;

import static com.getui.gtps.manufacturer.constant.ManufacturerConstants.ManufacturerName;

import java.io.File;
import java.net.URI;

/**
 * 厂商文件类，只比java.io.File多了manufacturerName属性
 *
 * date: 2021/1/5
 */
public class ManufacturerFile extends File {
    private final String manufacturerName;

    public ManufacturerFile(ManufacturerName manufacturerName, String pathname) {
        super(pathname);
        this.manufacturerName = manufacturerName.getName();
    }

    public ManufacturerFile(String manufacturerName, String parent, String child) {
        super(parent, child);
        this.manufacturerName = manufacturerName;
    }

    public ManufacturerFile(String manufacturerName, File parent, String child) {
        super(parent, child);
        this.manufacturerName = manufacturerName;
    }

    public ManufacturerFile(String manufacturerName, URI uri) {
        super(uri);
        this.manufacturerName = manufacturerName;
    }

    public String getManufacturerName() {
        return manufacturerName;
    }
}
