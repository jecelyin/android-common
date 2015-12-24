package com.jecelyin.android.common.utils;

import android.text.TextUtils;

/**
 * String工具类
 */
public class StringUtils {

    public static boolean isEmpty(String input) {
        return TextUtils.isEmpty(input);
    }
    /**
     * 检测两个字符串是否相同；
     */
    public final static boolean isSame(String value1, String value2) {
        if (isEmpty(value1) && isEmpty(value2)) {
            return true;
        } else if (!isEmpty(value1) && !isEmpty(value2)) {
            return (value1.trim().equalsIgnoreCase(value2.trim()));
        } else {
            return false;
        }
    }

    /**
     * 检测变量的值是否为一个整型数据；
     */
    public final static boolean isInt(String value) {
        if (isEmpty(value))
            return false;

        try {
            Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return false;
        }

        return true;
    }

    /**
     * 判断变量的值是否为double类型
     */
    public final static boolean isDouble(String value) {
        if (isEmpty(value))
            return false;
        try {
            Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }
    /**
     * 解析一个字符串为整数；
     */
    public final static int toInt(String value) {
        if (isInt(value))
            return Integer.parseInt(value);
        return 0;
    }

    public final static int toInt(String value, int defaultValue) {
        if (isInt(value))
            return Integer.parseInt(value);
        return defaultValue;
    }

    /**
     * 解析一个字符串为double
     */
    public final static double toDouble(String value) {
        return toDouble(value, 0);
    }

    public final static double toDouble(String value, double defaultValue) {
        if (isDouble(value))
            return Double.parseDouble(value);
        return defaultValue;
    }

    /**
     * 解析一个字符串为float
     */
    public static float toFloat(String value) {
        return toFloat(value, 0);
    }

    public static float toFloat(String value, float defaultValue) {
        try {
            return Float.parseFloat(value);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    /**
     * 对象转整数
     *
     * @param obj
     * @return 转换异常返回 0
     */
    public static long toLong(String obj) {
        try {
            return Long.parseLong(obj);
        } catch (Exception e) {
        }
        return 0;
    }

    /**
     * 字符串转布尔值
     *
     * @param b
     * @return 转换异常返回 false
     */
    public static boolean toBool(String b) {
        try {
            return Boolean.parseBoolean(b);
        } catch (Exception e) {
        }
        return false;
    }

    /**
     * 判定输入汉字
     * @param c
     * @return
     */
    public  final static boolean isChinese(char c) {
        Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
        if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
                || ub == Character.UnicodeBlock.GENERAL_PUNCTUATION
                || ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
                || ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS) {
            return true;
        }
        return false;
    }

    /**
     * Returns a string containing the tokens joined by delimiters.
     * @param tokens an array objects to be joined.
     */
    public static String join(CharSequence delimiter, int[] tokens) {
        StringBuilder sb = new StringBuilder();
        boolean firstTime = true;
        for (Object token: tokens) {
            if (firstTime) {
                firstTime = false;
            } else {
                sb.append(delimiter);
            }
            sb.append(token);
        }
        return sb.toString();
    }

}
