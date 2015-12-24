package com.jecelyin.android.common.http;

import com.squareup.okhttp.Response;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * @author Jecelyin <jecelyin@gmail.com>
 */
public abstract class OnHttpResponseListener<T> {
    final Type type;

    public OnHttpResponseListener() {
        Type superClass = getClass().getGenericSuperclass();
        type = ((ParameterizedType) superClass).getActualTypeArguments()[0];
    }

    protected Type getType() {
        return type;
    }

    /**
     * Called when the request could not be executed due to cancellation, a
     * connectivity problem or timeout. Because networks can fail during an
     * exchange, it is possible that the remote server accepted the request
     * before the failure.
     */
    public abstract void onFailure(HttpClient httpClient, HttpRequest request, Exception e);

    /**
     * Called when the HTTP response was successfully returned by the remote
     * server. The callback may proceed to read the response body with {@link
     * Response#body}. The response is still live until its response body is
     * closed with {@code response.body().close()}. The recipient of the callback
     * may even consume the response body on another thread.
     *
     * <p>Note that transport-layer success (receiving a HTTP response code,
     * headers and body) does not necessarily indicate application-layer
     * success: {@code response} may still indicate an unhappy HTTP response
     * code like 404 or 500.
     */
    public abstract void onResponse(HttpClient httpClient, HttpResponse response);
}
