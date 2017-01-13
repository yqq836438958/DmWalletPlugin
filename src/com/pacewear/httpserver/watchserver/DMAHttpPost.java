
package com.pacewear.httpserver.watchserver;

import android.util.Base64;

import com.pacewear.httpserver.HttpUrl;
import com.pacewear.httpserver.IHttpPost;
import com.tencent.tws.api.HttpRequestCommand;
import com.tencent.tws.api.HttpRequestGeneralParams;

import qrom.component.wup.base.utils.ZipUtils;

public class DMAHttpPost implements IHttpPost {
    public static final String TAG = "WatchHttpPost";
    private HttpRequestGeneralParams mParams = null;

    public DMAHttpPost() {
        mParams = new HttpRequestGeneralParams();
        mParams.setCacheResult(false, 0);
        mParams.setRequestTimeOut(3000);
        mParams.setUserAgent("tencent");
        mParams.addUrl(HttpUrl.get().getUrl());
        mParams.setRequestType(HttpRequestCommand.POST_WITH_STRAMRETURN);
    }

    @Override
    public void addHeader(String key, String val) {
        mParams.addHeader(key, val);
    }

    @Override
    public void addBody(byte[] dataPacket) {
        String data = Base64.encodeToString(dataPacket, Base64.DEFAULT);
        mParams.addBodyEntity(data, "base64-default");
    }

    @Override
    public Object get() {
        return mParams;
    }

}
