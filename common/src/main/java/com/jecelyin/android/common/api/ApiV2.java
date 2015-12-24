package com.jecelyin.android.common.api;

import android.content.Context;
import android.os.Build;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jecelyin.android.common.utils.CyptoUtils;
import com.jecelyin.android.common.utils.DeviceId;

import java.io.File;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

/**
 * @author Jecelyin <jecelyin@gmail.com>
 */
public abstract class ApiV2 extends AbstractApi {

    @Override
    public void handleParams(Context context, Map<String, Object> params) {

        Head head = new Head();
        head.clientKey = DeviceId.getDeviceId(context);
        head.sourceType = Build.MANUFACTURER + " " + Build.MODEL;
        head.mobileVersion = String.valueOf(Build.VERSION.SDK_INT);

        head.token = getToken(context);
        head.userId = getUserId(context);

        head.time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
        JSONObject jsonObject = new JSONObject();
        Class clazz = head.getClass();
        Field[] field = clazz.getDeclaredFields();
        try {
            for (Field f : field) {
                f.setAccessible(true);
                if (f.get(head) != null) {
                    jsonObject.put(f.getName(), f.get(head));
                }
            }
        } catch (IllegalArgumentException | IllegalAccessException e) {
            e.printStackTrace();
        }

        //move file field
        HashMap<String, Object> fileMap = new HashMap<>();
        for(Map.Entry<String, Object> entry : params.entrySet()) {
            Object value = entry.getValue();
            if(value instanceof File )
                fileMap.put(entry.getKey(), entry.getValue());
            else if(value instanceof Iterable) { // List<File>, Collection<File>, etc...
                Iterator iter = ((Iterable) value).iterator();
                if(iter.hasNext() && iter.next() instanceof File) {
                    fileMap.put(entry.getKey(), entry.getValue());
                }
            }
        }
        if(!fileMap.isEmpty())
            for(String key : fileMap.keySet()) {
                params.remove(key);
            }
        //end

        params.put("head", jsonObject);
        String paramJson = JSON.toJSONString(params);
        final String key = "fangdr";
        String sign = CyptoUtils.md5(paramJson + key);
        params.clear();
        params.put("params", paramJson);
        params.put("sign", sign);
        params.putAll(fileMap);
    }

    protected abstract String getUserId(Context context);

    protected abstract String getToken(Context context);
}
