package com.getui.gtps.util;

/**
 * Http调用返回结果包装类
 *
 * date: 2020/12/28
 */
public class HttpResponse {

    private final Integer status;
    private final String message;
    private final String content;

    HttpResponse(Integer status, String message, String content) {
        this.status = status;
        this.message = message;
        this.content = content;
    }

    public Integer getStatus() {
        return this.status;
    }

    public String getMessage() {
        return this.message;
    }

    public String getContent() {
        return this.content;
    }

    public String toString() {
        return "{status:" + status + ","
                + "message:" + message + ","
                + "content:" + content + "}";
    }

    public boolean success() {
        return status == 200;
    }

    public static HttpResponse fail(String content) {
        return new HttpResponse(0, "fail", content);
    }

    public static HttpResponse success(String content) {
        return new HttpResponse(200, "success", content);
    }
}
