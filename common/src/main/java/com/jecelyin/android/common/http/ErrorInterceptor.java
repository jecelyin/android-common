package com.jecelyin.android.common.http;

import android.content.Context;

/**
 * @author Jecelyin <jecelyin@gmail.com>
 * 网络错误拦截器，主要处理登录过期
 * 在AndroidManifest.xml的Application标签下添加
 * <meta-data android:name="http_error_interceptor" android:value=".helper.HttpErrorInterceptor" />
 */
public interface ErrorInterceptor {
    boolean onError(Context context, Exception e);
}
