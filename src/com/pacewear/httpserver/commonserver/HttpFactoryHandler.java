
package com.pacewear.httpserver.commonserver;

import android.content.Context;

import com.pacewear.httpserver.IHttpPost;
import com.pacewear.httpserver.ServerHandler;
import com.pacewear.httpserver.commonserver.IHttpFactoryImpl.IHttpFcatoryCallback;
import com.qq.jce.wup.UniPacket;

import org.apache.http.client.methods.HttpPost;

import qrom.component.wup.base.utils.ZipUtils;

public class HttpFactoryHandler extends ServerHandler {
    private IHttpFactoryImpl mHttpFactoryImpl = null;

    public HttpFactoryHandler(Context context) {
        super(context, new CommonHttpPost());
    }

    @Override
    public int reqServer(final int operType, UniPacket uniPacket) {
        wrapHttpPost(uniPacket.encode());
        IHttpFcatoryCallback callback = new IHttpFcatoryCallback() {

            @Override
            public void onSuccess(byte[] rspData) {
                byte[] result = ZipUtils.unGzip(rspData);
                onResponseSucceed(00, operType, result);
            }

            @Override
            public void onFail(int error, String desc) {
                onResponseFailed(0, operType, error, desc);

            }
        };
        return mHttpFactoryImpl.doRequest((HttpPost) getHttpPostContent(), callback);
    }

    @Override
    public boolean isTestEnv() {
        return mHttpFactoryImpl.isTestEnv();
    }

}
