package com.getui.gtps.manufacturer;

import java.io.Serializable;

/**
 * 厂商服务返回结果，此类是厂商api调用结果的包装类
 *
 * @author wangxu
 * date: 2020/12/31
 * email：wangx2@getui.com
 */
public class Result implements Serializable {

    private static final long serialVersionUID = 2383656123926111267L;

    public final static int success = 0;
    public final static int fail = 1;
    public final static int timeout = 2;
    public final static int noInstance = 3;
    public final static int authFail = 4;
    public final static String SUCCESS = "success";
    public final static String FAIL = "fail";
    public final static String TIMEOUT = "timeout";
    public final static String NO_INSTANCE = "has no manufacturer instance";
    public final static String AUTH_FAIL = "auth fail";

    private final int code;
    private final String message;
    private final String data;

    public Result(int code, String message, String data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public String getData() {
        return data;
    }

    public boolean success() {
        return this.code == success;
    }

    public static Result success(String data) {
        return new Result(success, SUCCESS, data);
    }

    public static Result fail(String data) {
        return new Result(fail, FAIL, data);
    }

    public static Result timeout() {
        return new Result(timeout, TIMEOUT, null);
    }

    public static Result noInstance() {
        return new Result(noInstance, NO_INSTANCE, null);
    }

    public static Result authFail() {
        return new Result(authFail, AUTH_FAIL, null);
    }


    @Override
    public String toString() {
        return "{code:" + code + ","
                + "message:" + message + ","
                + "data:" + data + "}";
    }
}
