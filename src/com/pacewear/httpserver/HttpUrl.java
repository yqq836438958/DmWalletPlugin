
package com.pacewear.httpserver;

public class HttpUrl {
    private static HttpUrl sInstance = null;

    public static HttpUrl get() {
        if (sInstance == null) {
            synchronized (HttpUrl.class) {
                sInstance = new HttpUrl();
            }
        }
        return sInstance;
    }

    private HttpUrl() {

    }

    public String getUrl() {
        return "http://wtest.html5.qq.com:55555";
    }

    public String getHttpHost() {
        return "wtest.html5.qq.com:55555";
    }
}
