package com.jecelyin.android.common.api;

import android.content.Context;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Jecelyin <jecelyin@gmail.com>
 */
public abstract class AbstractApi {
    private int page;
    private int pageSize = 15;
    public static String API_URL = "";

    public static class Head {
        public String apiVersion = "1.0";
        /* 设备唯一标识md5 */
        public String clientKey;
        public String userId;
        /* 2 : Android */
        public int mobileType = 2;
        /* 品牌类型 */
        public String sourceType;
        /* 系统版本 */
        public String mobileVersion;
        /* 登陆成功后由后台返回,后台每次都需要进行判断该连接是否有效 */
        public String token;
        /* 请求时间 */
        public String time, timeNow;
    }

    public static enum Method {
        GET,
        POST,
    }

    protected abstract String getPath();
    public abstract Method requestMethod();

    public String getUrl() {
        return API_URL + getPath();
    }

    public void setPage(int page) {
        this.page = page;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public Map<String, Object> getParams() {
        HashMap<String, Object> params = new HashMap<String, Object>();
        Class clazz = getClass();
        Field[] field = clazz.getDeclaredFields();
        try {
            for (Field f : field) {
                f.setAccessible(true);
                Object value = f.get(this);
                if (value != null) {
                    params.put(f.getName(), value);
                }
            }
        } catch (IllegalArgumentException | IllegalAccessException e) {
            e.printStackTrace();
        }

        if(page > 0) {
            params.put("currPage", page); //dev
            params.put("page", page); //tuike
            params.put("pageSize", pageSize);
        }

        return params;
    }

    public void handleParams(Context context, Map<String, Object> params) {}
}
