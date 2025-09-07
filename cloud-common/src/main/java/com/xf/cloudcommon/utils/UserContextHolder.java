package com.xf.cloudcommon.utils;


import java.util.Map;

/**
 * UserContextHolder
 *
 * @author 海言
 * @date 2025/9/5
 * @time 15:57
 * @Description 请求头工具类
 */
public class UserContextHolder {

    private static final ThreadLocal<Map<String, String>> context = new ThreadLocal<>();
    // 设置用户信息
    public static void set(Map<String, String> userInfo) {
        context.set(userInfo);
    }

    // 获取用户信息
    public static Map<String, String> get() {
        return context.get();
    }

    public static String getUserId() {
        Map<String, String> userInfo = context.get();
        return userInfo != null ? userInfo.get("id") : null;
    }

    public static String getName() {
        Map<String, String> userInfo = context.get();
        return userInfo != null ? userInfo.get("name") : null;
    }
    // 清理
    public static void clear() {
        context.remove();
    }

}
