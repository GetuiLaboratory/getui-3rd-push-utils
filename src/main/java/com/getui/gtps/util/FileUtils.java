package com.getui.gtps.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 文件工具类
 *
 * @author wangxu
 * date: 2020/12/31
 * email：wangx2@getui.com
 */
public class FileUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileUtils.class);

    /**
     * 计算指定文件sha1值
     *
     * @param file 指定文件
     * @return sha1值
     */
    public static String sha1(File file) {
        try (InputStream in = new FileInputStream(file)) {
            byte[] buffer = new byte[1024 * 1024 * 10];
            MessageDigest digest = MessageDigest.getInstance("SHA1");
            int len;
            while ((len = in.read(buffer)) > 0) {
                digest.update(buffer, 0, len);
            }
            return toHex(digest.digest());
        } catch (NoSuchAlgorithmException | IOException e) {
            LOGGER.error("get file sha1 fail. ", e);
        }
        return file.getName();
    }

    // 将传递进来的字节数组转换成十六进制的字符串形式并返回
    private static String toHex(byte[] buffer) {
        StringBuilder sb = new StringBuilder(buffer.length * 2);
        for (byte b : buffer) {
            sb.append(Character.forDigit((b & 0xf0) >> 4, 16));
            sb.append(Character.forDigit(b & 0x0f, 16));
        }
        return sb.toString();
    }
}
