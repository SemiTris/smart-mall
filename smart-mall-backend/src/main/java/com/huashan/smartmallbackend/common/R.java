package com.huashan.smartmallbackend.common;

import lombok.Data;

/**
 * 统一返回结果封装
 * <p>
 * 所有 Controller 方法统一返回 R&lt;T&gt;，前端 request.js 拿到的是本对象本身
 * （其响应拦截器返回 response.data），因此前端需自行判断 code === 200。
 *
 * @param <T> 泛型
 * @author hs
 */
@Data
public class R<T> {

    /**
     * 状态码：200 成功，其余为失败
     */
    private int code;

    /**
     * 提示消息
     */
    private String msg;

    /**
     * 返回数据
     */
    private T data;

    /**
     * 成功响应（无数据）
     *
     * @param <T> 泛型
     * @return R 实例
     */
    public static <T> R<T> ok() {
        return ok(null);
    }

    /**
     * 成功响应（带数据）
     *
     * @param data 数据
     * @param <T>  泛型
     * @return R 实例
     */
    public static <T> R<T> ok(T data) {
        R<T> r = new R<>();
        r.setCode(200);
        r.setMsg("操作成功");
        r.setData(data);
        return r;
    }

    /**
     * 成功响应（带消息和数据）
     *
     * @param msg  消息
     * @param data 数据
     * @param <T>  泛型
     * @return R 实例
     */
    public static <T> R<T> ok(String msg, T data) {
        R<T> r = new R<>();
        r.setCode(200);
        r.setMsg(msg);
        r.setData(data);
        return r;
    }

    /**
     * 失败响应（默认 500）
     *
     * @param msg 错误消息
     * @param <T> 泛型
     * @return R 实例
     */
    public static <T> R<T> fail(String msg) {
        return fail(500, msg);
    }

    /**
     * 失败响应（指定状态码）
     *
     * @param code 状态码
     * @param msg  错误消息
     * @param <T>  泛型
     * @return R 实例
     */
    public static <T> R<T> fail(int code, String msg) {
        R<T> r = new R<>();
        r.setCode(code);
        r.setMsg(msg);
        return r;
    }
}