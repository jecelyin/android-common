package com.jecelyin.android.common.http;

import com.squareup.okhttp.Request;

import java.util.Map;

/**
 * @author Jecelyin <jecelyin@gmail.com>
 */
public final class HttpRequest {
    public final String url;
    public final String method;
    public final HttpHeaders headers;
    public final Map<String, Object> body;
    public final String tag;

    HttpRequest(String url, String method, HttpHeaders headers, Map<String, Object> body, String tag) {
        this.url = url;
        this.method = method;
        this.headers = headers;
        this.body = body;
        this.tag = tag;
    }

    HttpRequest(Request request, Map<String, Object> data, String tag) {
        this.url = request.urlString();
        this.method = request.method();
        this.headers = new HttpHeaders.Builder(request.headers()).build();
        this.body = data;
        this.tag = tag;
    }
}
