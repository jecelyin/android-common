package com.jecelyin.android.common.http;

import com.squareup.okhttp.Response;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static java.net.HttpURLConnection.HTTP_MOVED_PERM;
import static java.net.HttpURLConnection.HTTP_MOVED_TEMP;
import static java.net.HttpURLConnection.HTTP_MULT_CHOICE;
import static java.net.HttpURLConnection.HTTP_SEE_OTHER;

/**
 * An HTTP response. Instances of this class are not immutable: the response
 * body is a one-shot value that may be consumed only once. All other properties
 * are immutable.
 */
public final class HttpResponse {
    private final HttpRequest request;
    private final int code;
    private final String message;
    private final String tag;
    private final HttpHeaders headers;
    private final HttpResponseBody body;
    private boolean isCacheResponse;

    private HttpResponse(Builder builder) {
        this.request = builder.request;
        this.code = builder.code;
        this.message = builder.message;
        this.tag = builder.tag;
        this.headers = builder.headers.build();
        this.body = builder.body;
        this.isCacheResponse = builder.isCacheResponse;
    }

    public boolean isCacheResponse() {
        return isCacheResponse;
    }

    public void setIsCacheResponse(boolean isCacheResponse) {
        this.isCacheResponse = isCacheResponse;
    }

    /**
     * The wire-level request that initiated this HTTP response. This is not
     * necessarily the same request issued by the application:
     * <ul>
     *     <li>It may be transformed by the HTTP client. For example, the client
     *         may copy headers like {@code Content-Length} from the request body.
     *     <li>It may be the request generated in response to an HTTP redirect or
     *         authentication challenge. In this case the request URL may be
     *         different than the initial request URL.
     * </ul>
     */
    public HttpRequest request() {
        return request;
    }

    /** Returns the HTTP status code. */
    public int code() {
        return code;
    }

    /**
     * Returns true if the code is in [200..300), which means the request was
     * successfully received, understood, and accepted.
     */
    public boolean isSuccessful() {
        return code >= 200 && code < 300;
    }

    /** Returns the HTTP status message or null if it is unknown. */
    public String message() {
        return message;
    }

    public String tag() {
        return tag;
    }

    public List<String> headers(String name) {
        return headers.values(name);
    }

    public String header(String name) {
        return header(name, null);
    }

    public String header(String name, String defaultValue) {
        String result = headers.get(name);
        return result != null ? result : defaultValue;
    }

    public HttpHeaders headers() {
        return headers;
    }

    public HttpResponseBody body() {
        return body;
    }

    /** Returns true if this response redirects to another resource. */
    public boolean isRedirect() {
        switch (code) {
            case HTTP_MULT_CHOICE:
            case HTTP_MOVED_PERM:
            case HTTP_MOVED_TEMP:
            case HTTP_SEE_OTHER:
                return true;
            default:
                return false;
        }
    }

    @Override public String toString() {
        return "Response{code="
                + code
                + ", message="
                + message
                + ", url="
                + request.url
                + '}';
    }

    public static class Builder {
        private HttpRequest request;
        private int code = -1;
        private String message;
        private String tag;
        private HttpHeaders.Builder headers;
        private HttpResponseBody body;
        private boolean isCacheResponse;

        public Builder() {
            headers = new HttpHeaders.Builder();
        }

        public Builder(Response response, Map<String, Object> data, String tag) throws IOException {
            this();
            request = new HttpRequest(response.request(), data, tag);
            code = response.code();
            message = response.message();
            this.tag = tag;
            headers = new HttpHeaders.Builder(response.headers());
            body = new HttpResponseBody(response.body().bytes());
        }

        public Builder request(HttpRequest request) {
            this.request = request;
            return this;
        }

        public Builder isCacheResponse(boolean b) {
            this.isCacheResponse = b;
            return this;
        }

        public Builder code(int code) {
            this.code = code;
            return this;
        }

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public Builder tag(String tag) {
            this.tag = tag;
            return this;
        }

        /**
         * Sets the header named {@code name} to {@code value}. If this request
         * already has any headers with that name, they are all replaced.
         */
        public Builder header(String name, String value) {
            headers.set(name, value);
            return this;
        }

        /**
         * Adds a header with {@code name} and {@code value}. Prefer this method for
         * multiply-valued headers like "Set-Cookie".
         */
        public Builder addHeader(String name, String value) {
            headers.add(name, value);
            return this;
        }

        public Builder removeHeader(String name) {
            headers.removeAll(name);
            return this;
        }

        public Builder body(HttpResponseBody body) {
            this.body = body;
            return this;
        }

        public HttpResponse build() {
            if (request == null) throw new IllegalStateException("request == null");
            if (code < 0) throw new IllegalStateException("code < 0: " + code);
            return new HttpResponse(this);
        }
    }
}
