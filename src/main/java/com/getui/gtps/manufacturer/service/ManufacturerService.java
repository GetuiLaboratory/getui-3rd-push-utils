package com.getui.gtps.manufacturer.service;

import com.getui.gtps.manufacturer.Result;

import java.io.File;

/**
 * 厂商服务接口，定义了多厂商共有的服务
 *
 * @author wangxu
 * date: 2020/12/25
 * email：wangx2@getui.com
 */
public interface ManufacturerService {

    /**
     * 厂商icon上传
     *
     * @param file 本地icon文件
     * @return 上传icon结果
     */
    Result uploadIcon(File file);

    /**
     * 厂商图片上传
     *
     * @param file 本地图片文件
     * @return 上传pic结果
     */
    Result uploadPic(File file);
}
