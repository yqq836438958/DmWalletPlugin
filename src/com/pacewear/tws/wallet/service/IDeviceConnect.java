
package com.pacewear.tws.wallet.service;

import com.tws.plugin.aidl.IPaceCallBack;
import com.tws.plugin.aidl.PaceInfo;

public interface IDeviceConnect {
    public void init();

    public void deinit();

    public void connect(String macId);

    public boolean isConnected();

    public void getDeviceInfo(PaceInfo info);

    public void disConnect();

    public void scan();

    public void addCallback(IPaceCallBack callback);

    public void rmCallback(IPaceCallBack callback);
}
