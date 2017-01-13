
package com.pacewear.httpserver;

public interface IHttpPost {
    public void addHeader(String key, String val);

    public void addBody(byte[] dataPacket);

    public Object get();
}
