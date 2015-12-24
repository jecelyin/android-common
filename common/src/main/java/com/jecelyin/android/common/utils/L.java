package com.jecelyin.android.common.utils;

import android.util.Log;

/**
 * @author Jecelyin <jecelyin@gmail.com>
 */
public class L {
    public static boolean debug = false;

    public static int v(String tag, String msg) {
        if(!debug)
            return 0;
        return Log.v(tag, msg);
    }

    public static int v(String tag, String msg, Throwable tr) {
        if(!debug)
            return 0;
        return Log.v(tag, msg, tr);
    }

    public static int d(String tag, String msg) {
        if(!debug)
            return 0;
        return Log.d(tag, msg);
    }

    public static int d(String tag, String msg, Throwable tr) {
        if(!debug)
            return 0;
        return Log.d(tag, msg, tr);
    }

    public static int d(String format, Object... args) {
        return d("TK_LOG_D", String.format(format, args));
    }

    public static int d(Throwable t) {
        return d("TK_LOG_D", t.getMessage(), t);
    }

    public static int i(String tag, String msg) {
        if(!debug)
            return 0;
        return Log.i(tag, msg);
    }

    public static int i(String tag, String msg, Throwable tr) {
        if(!debug)
            return 0;
        return Log.i(tag, msg, tr);
    }

    public static int w(String tag, String msg) {
        return Log.w(tag, msg);
    }

    public static int w(String tag, String msg, Throwable tr) {
        return Log.w(tag, msg, tr);
    }

    public static int w(String tag, Throwable tr) {
        return Log.w(tag, tr);
    }

    public static int e(String tag, String msg) {
        return Log.e(tag, msg);
    }

    public static int e(String tag, String msg, Throwable tr) {
        return Log.e(tag, msg, tr);
    }

    public static int e(String format, Object... args) {
        return e("TK_LOG_E", String.format(format, args));
    }

    public static int e(Throwable t) {
        return Log.e("TK_LOG_E", t.getMessage(), t);
    }
}
