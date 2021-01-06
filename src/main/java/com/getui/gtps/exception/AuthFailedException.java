package com.getui.gtps.exception;

/**
 * 厂商服务鉴权失败的异常
 *
 * @author wangxu
 * date: 2020/12/30
 * email：wangx2@getui.com
 */
public class AuthFailedException extends RuntimeException {

    public AuthFailedException(String name, Throwable cause) {
        super(name + " auth fail. ", cause);
    }
}

