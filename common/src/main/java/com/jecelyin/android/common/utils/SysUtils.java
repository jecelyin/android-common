package com.jecelyin.android.common.utils;

import android.content.Context;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import java.io.File;
import java.lang.reflect.Field;

/**
 * @author Jecelyin <jecelyin@gmail.com>
 */
public class SysUtils {
    /**
     * Gets a field from the project's BuildConfig. This is useful when, for example, flavors
     * are used at the project level to set custom fields.
     * @param context       Used to find the correct file
     * @param fieldName     The name of the field-to-access
     * @return              The value of the field, or {@code null} if the field is not found.
     */
    public static Object getBuildConfigValue(Context context, String fieldName) {
        try {
            Class<?> clazz = Class.forName(context.getPackageName() + ".BuildConfig");
            Field field = clazz.getField(fieldName);
            return field.get(null);
        } catch (Exception e) {
//            L.d(e);
        }
        return null;
    }

    /**
     * Android studio 多个module时，非当前运行的module对家获取BuildConfig.DEBUG都是false
     * 这里通过获取当前应用的context下的BuildConfig来判断才正确
     * @param context
     * @return
     */
    public static boolean isDebug(Context context) {
        Object result = getBuildConfigValue(context, "DEBUG");
        return result != null && ((boolean)result);
    }

    public static File getSDCardDir() {
        return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) ? Environment.getExternalStorageDirectory() : null;
    }

    public static boolean isAutoTester(Context context) {
        Object result = getBuildConfigValue(context, "AUTO_TEST");
        return result != null && ((boolean)result);
    }

    public static int getScreenWidth(Context context) {
        try {
            DisplayMetrics metrics = new DisplayMetrics();
            WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            wm.getDefaultDisplay().getMetrics(metrics);
            return metrics.widthPixels;
        } catch (Exception e) {
            L.e(e);
            return 0;
        }
    }
}
