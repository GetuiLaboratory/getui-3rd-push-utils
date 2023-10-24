package com.getui.gtps.exception;

/**
 * 厂商服务鉴权失败的异常
 *
 * date: 2020/12/30
 */
public class AuthFailedException extends RuntimeException {

    public AuthFailedException(String name, Throwable cause) {
        super(name + " auth fail. ", cause);
    }
}

