package com.jecelyin.android.common.http;

import android.content.Context;
import android.content.DialogInterface;
import android.text.TextUtils;
import android.webkit.MimeTypeMap;

import com.jecelyin.android.common.api.AbstractApi;
import com.jecelyin.android.common.utils.L;
import com.jecelyin.android.common.utils.SysUtils;
import com.jecelyin.android.common.widget.ProgressHUD;
import com.squareup.okhttp.Cache;
import com.squareup.okhttp.CacheControl;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.MultipartBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Jecelyin <jecelyin@gmail.com>
 */
public class HttpClient {
    private static OkHttpClient client;
    private final Context context;
    private ProgressHUD mProgressHUD;
    private boolean useCache = false;
    private static boolean debug;
    private static AtomicInteger requestCount = new AtomicInteger(0);

    private HttpClient(Context context) {
        this.context = context;
        debug = SysUtils.isDebug(context);
    }

    public synchronized static HttpClient newInstance(Context context) {
        if(client == null) {
            int cacheSize = 10 * 1024 * 1024; // 10 MiB
            File cacheDirectory = new File(context.getCacheDir(), "http");
            Cache cache = new Cache(cacheDirectory, cacheSize);
            client = new OkHttpClient();
            client.setCache(cache);
            client.interceptors().add(new GzipRequestInterceptor());
            client.setReadTimeout(180, TimeUnit.SECONDS);
            client.setConnectTimeout(30, TimeUnit.SECONDS);
        }

        return new HttpClient(context);
    }

    public Context getContext() {
        return context;
    }

    private String getTagAndCount() {
        int num = requestCount.incrementAndGet();
        return "HttpRequest-"+num;
    }

    public void setUseCache(boolean useCache) {
        this.useCache = useCache;
    }

    public boolean isDebug() {
        return debug;
    }

    public void cancel(String tag) {
        if(TextUtils.isEmpty(tag))
            return;
        client.cancel(tag);
    }

    public String request(AbstractApi api) {
        return request(api, null);
    }

    public String request(AbstractApi api, OnHttpResponseListener listener) {
        String tag = getTagAndCount();
        request(tag, api, listener);
        return tag;
    }

    public void request(String tag, AbstractApi api, OnHttpResponseListener listener) {
        String url = api.getUrl();
        Map<String, Object> params = api.getParams();
        api.handleParams(context, params);

        String method;
        if(api.requestMethod() == AbstractApi.Method.POST)
        {
            method = "POST";
        }else{
            method = "GET";
        }

        request(tag, method, url, null, params, listener);
    }

    public String loadingRequest(AbstractApi api) {
        return loadingRequest(api, null, null);
    }

    public String loadingRequest(AbstractApi api, OnHttpResponseListener listener) {
        return loadingRequest(api, listener, null);
    }

    public String loadingRequest(AbstractApi api, OnHttpResponseListener listener, String loadingText) {
        final String tag = getTagAndCount();
        try {
            mProgressHUD = ProgressHUD.show(context, loadingText, true, true, new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    client.cancel(tag);
                }
            });
        } catch (Exception e) {
            //java.lang.IllegalArgumentException: View=com.android.internal.policy.impl.PhoneWindow$DecorView{43b81b28 V.E..... R.....ID 0,0-396,341} not attached to window manager
            L.d(e);
        }
        request(tag, api, listener);
        return tag;
    }

    private static String mapToQueryString(Map<String, Object> params) {
        StringBuilder encodedParams = new StringBuilder();
        try {
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                if(entry.getValue() == null || entry.getValue() instanceof File)
                    continue;
                encodedParams.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
                encodedParams.append('=');
                encodedParams.append(URLEncoder.encode(String.valueOf(entry.getValue()), "UTF-8"));
                encodedParams.append('&');
            }
            return encodedParams.toString();
        } catch (UnsupportedEncodingException uee) {
            throw new RuntimeException("Encoding not supported: UTF-8", uee);
        }
    }

    public void request(final String tag, String method, String url, final HttpHeaders headers, final Map<String, Object> data, final OnHttpResponseListener listener) {
        Request.Builder builder = new Request.Builder();

        if("POST".equals(method))
        {

            builder.post(data2RequestBody(data));
        }else{
            if(data != null && !data.isEmpty()) {
                url += "?" + mapToQueryString(data);
            }
            builder.get();
        }

        if(headers != null && headers.size() > 0) {
            int size = headers.size();
            for (int i = 0; i < size; i++) {
                builder.addHeader(headers.name(i), headers.value(i));
            }
        }

        final Request request;
        if(useCache) {
            request = builder.tag(tag)
                    .url(url)
                    .cacheControl(CacheControl.FORCE_CACHE)
                    .build();
            final Request networkRequest = builder.cacheControl(CacheControl.FORCE_NETWORK).build();
            //读取完缓存结果后，再读取网络
            request(request, data, new OnHttpResponseListener() {
                @Override
                public void onFailure(HttpClient httpClient, HttpRequest r, Exception e) {
                    if(debug) {
                        L.e(e);
                    }

                    request(networkRequest, data, listener);
                }

                @Override
                public void onResponse(HttpClient httpClient, HttpResponse response) {
                    if(response.isSuccessful()){
                        if(listener != null){
                            response.setIsCacheResponse(true);
                            listener.onResponse(httpClient, response);
                        }
                    }
                    request(networkRequest, data, listener);
                }
            });
        } else {
            request = builder.tag(tag)
                    .url(url)
                    .cacheControl(CacheControl.FORCE_NETWORK)
                    .build();
            request(request, data, listener);
        }
    }

    private void request(Request request, final Map<String, Object> data, final OnHttpResponseListener listener) {
        final String tag = (String)request.tag();

        if(request.cacheControl() != CacheControl.FORCE_CACHE) {
            L.d("BeanRequest", "URL=" + request.urlString() + " POST=" + data);
        }

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                onResponded();
                HttpRequest hr = new HttpRequest(request, data, tag);
                listener.onFailure(HttpClient.this, hr, e);
            }

            @Override
            public void onResponse(Response response) throws IOException {
                onResponded();
                HttpResponse.Builder b = new HttpResponse.Builder(response, data, tag);
                listener.onResponse(HttpClient.this, b.build());
            }
        });
    }

    private void onResponded() {
        if(mProgressHUD != null) {
            try {
                mProgressHUD.dismiss();
            } catch (Exception e) {
                L.e(e);
            } finally {
                mProgressHUD = null;
            }
        }
    }

    private static RequestBody data2RequestBody(Map<String, Object> data) {
        if(data == null || data.isEmpty())
            return null;

        MultipartBuilder mb = new MultipartBuilder();
        String key;
        Object value;

        for(Map.Entry<String, Object> entry : data.entrySet()) {
            key = entry.getKey();
            value = entry.getValue();

            if(value instanceof File) {
                File file = (File) value;
                mb.addFormDataPart(key, file.getName(), RequestBody.create(MediaType.parse(getMimeType(file.getName())), file));

            } else if(value instanceof Iterable) {
                Iterator iter = ((Iterable) value).iterator();
                Object item;

                for(; iter.hasNext(); ) {
                    item = iter.next();

                    if(item instanceof File) {
                        File file = (File) item;
                        mb.addFormDataPart(key, file.getName(), RequestBody.create(MediaType.parse(getMimeType(file.getName())), file));
                    } else {
                        mb.addFormDataPart(key, String.valueOf(value));
                    }
                }

            } else {
                mb.addFormDataPart(key, String.valueOf(value));
            }
        }
        return mb.build();
    }

    public static String getMimeType(String url) {
        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        if(TextUtils.isEmpty(type)) {
            type = "application/octet-stream";
        }

        return type;
    }
}
