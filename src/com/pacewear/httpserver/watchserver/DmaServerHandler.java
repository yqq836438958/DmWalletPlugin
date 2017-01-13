
package com.pacewear.httpserver.watchserver;

import android.content.Context;
import android.util.Log;

import com.pacewear.httpserver.ServerHandler;
import com.qq.jce.wup.UniPacket;
import com.tencent.tws.api.HttpManager;
import com.tencent.tws.api.HttpRequestGeneralParams;
import com.tencent.tws.api.HttpResponseListener;
import com.tencent.tws.api.HttpResponseResult;

public class DmaServerHandler extends ServerHandler {
    protected static final String TAG = "DmaServerHandler";
    private HttpManager mHttpManager = null;

    public DmaServerHandler(Context context) {
        super(context, new DMAHttpPost());
        mHttpManager = HttpManager.getInstance(context);
    }

    @Override
    public int reqServer(final int operType, UniPacket uniPacket) {
        wrapHttpPost(uniPacket.encode());
        mHttpManager.postGeneralHttpRequest((HttpRequestGeneralParams) getHttpPostContent(),
                new HttpResponseListener() {

                    @Override
                    public void onResponse(HttpResponseResult arg0) {
                        String data = arg0.mData;
                        Log.e(TAG, "onResponse:" + data);
                        onResponseSucceed(00, operType, onParseHttpRsp(data));
                    }

                    @Override
                    public void onError(int arg0, HttpResponseResult arg1) {
                        Log.e(TAG, "onError:" + arg0 + ",desc:" + arg1.mData);
                        onResponseFailed(00, operType, arg0, "");
                    }
                });
        return 0;
    }

    @Override
    public boolean isTestEnv() {
        return false;
    }
}
