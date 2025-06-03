package com.wyc.utils;

import lombok.Data;
import java.io.Serializable;

@Data
public class R<T> implements Serializable {
    private static final long serialVersionUID = 1L;

    private int code;
    private String msg;
    private T data;

    public static <T> R<T> ok() {
        return restResult(null, 200, "操作成功");
    }

    public static <T> R<T> ok(T data) {
        return restResult(data, 200, "操作成功");
    }

    public static <T> R<T> ok(String msg) {
        return restResult(null, 200, msg);
    }

    public static <T> R<T> ok(String msg, T data) {
        return restResult(data, 200, msg);
    }

    public static <T> R<T> error() {
        return restResult(null, 500, "操作失败");
    }

    public static <T> R<T> error(String msg) {
        return restResult(null, 500, msg);
    }

    public static <T> R<T> error(int code, String msg) {
        return restResult(null, code, msg);
    }

    private static <T> R<T> restResult(T data, int code, String msg) {
        R<T> r = new R<>();
        r.setCode(code);
        r.setData(data);
        r.setMsg(msg);
        return r;
    }
}
