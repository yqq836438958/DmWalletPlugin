
package com.pacewear.httpserver.commonserver;

import org.apache.http.client.methods.HttpPost;

public interface IHttpFactoryImpl {
    public static interface IHttpFcatoryCallback {
        public void onSuccess(byte[] rspData);

        public void onFail(int error, String desc);
    }

    public int doRequest(HttpPost httpData, IHttpFcatoryCallback callback);

    public boolean isTestEnv();
}
