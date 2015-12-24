package com.jecelyin.android.common.http;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.jecelyin.android.common.R;
import com.jecelyin.android.common.bean.BaseBean;
import com.jecelyin.android.common.utils.L;

import java.lang.reflect.Type;

/**
 * @author Jecelyin <jecelyin@gmail.com>
 */
public abstract class BeanResponse<T extends BaseBean> extends OnHttpResponseListener<T> {

    @Override
    public void onResponse(final HttpClient httpClient, final HttpResponse response) {
        new AsyncTask<Type, Void, T>() {

            @Override
            protected T doInBackground(Type... params) {
                try {
                    String jsonString = new String(response.body().bytes(), "UTF-8");
                    if(!response.isCacheResponse()) {
                        L.d("BeanRequest", "Response=" + jsonString);
                    }
                    return JSON.parseObject(jsonString, getType());
                } catch (Exception e) {
                    L.e(e);
                    return null;
                }
            }

            @Override
            protected void onPostExecute(T bean) {
                //new api: status响应结果，0表示请求成功，1表示请求失败，2表示需登录但未登录或登录超时
                if(!"ok".equals(bean.getStatus()) && !"0".equals(bean.getStatus())) {
                    onFailure(httpClient, response.request(), new BeanStatusException(TextUtils.isEmpty(bean.getErrorMsg()) ? bean.getMessage() : bean.getErrorMsg(), bean));
                    return;
                }
                onResponse(httpClient, response, bean);
            }
        }.execute();
    }

    public abstract void onResponse(final HttpClient httpClient, final HttpResponse response, T bean);

    @Override
    public void onFailure(HttpClient httpClient, HttpRequest request, Exception e) {
        Context context = httpClient.getContext();
        if(!httpClient.isDebug() || TextUtils.isEmpty(e.getMessage())) {
            // context.getString(R.string.http_request_timeout)
            e = new Exception(context.getString(R.string.http_request_error), e);
        } else {
            L.e(e);
        }

        ApplicationInfo appInfo = null;
        try {
            appInfo = context.getPackageManager().getApplicationInfo(
                    context.getPackageName(), PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException ee) {
            L.e(ee);
        }
        if(appInfo != null && appInfo.metaData != null) {
            String interceptor = appInfo.metaData.getString("http_error_interceptor");
            if(!TextUtils.isEmpty(interceptor)) {
                if(interceptor.startsWith(".")) {
                    interceptor = appInfo.packageName + interceptor;
                }
                try {
                    ErrorInterceptor errorInterceptor = (ErrorInterceptor) Class.forName(interceptor).newInstance();
                    if(errorInterceptor.onError(context, e))
                        return;
                } catch (Exception e2) {
                    L.e(e2);
                }
            }
        }
        Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
    }
}
