
package com.pacewear.httpserver.commonserver;

import com.pacewear.httpserver.IHttpPost;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;

public class CommonHttpPost implements IHttpPost {

    private HttpPost mClientHttpPost = null;

    public CommonHttpPost() {
    }

    @Override
    public void addHeader(String key, String val) {
        mClientHttpPost.addHeader(key, val);
    }

    @Override
    public void addBody(byte[] dataPacket) {
        mClientHttpPost.setEntity(new ByteArrayEntity(dataPacket));
    }

    @Override
    public Object get() {
        return mClientHttpPost;
    }

}
