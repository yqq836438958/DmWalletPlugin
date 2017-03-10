
package com.pacewear.tws.wallet.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.tws.plugin.aidl.PaceServiceAIDL;
import com.tws.plugin.aidl.PaceInfo;
import com.pacewear.tws.phoneside.wallet.common.ByteUtil;
import com.tws.plugin.aidl.IPaceCallBack;
import java.util.concurrent.atomic.AtomicBoolean;

public class PaceApduService extends Service {
    public static final String TAG = PaceApduService.class.getSimpleName();
    private ISeInvoker mSeInvoker = new WalletSeInvoker();
    private IDeviceConnect mDeviceConnection = null;

    @Override
    public IBinder onBind(Intent intent) {
        printf("service onBind");
        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        printf("service on Create:" + Thread.currentThread().getName());
        mDeviceConnection = new PaceDeviceConnect(getApplicationContext());
        mDeviceConnection.init();
    }

    @Override
    public void onDestroy() {
        printf("service on destroy");
        mDeviceConnection.deinit();
        super.onDestroy();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        printf("service on unbind");
        return super.onUnbind(intent);
    }

    public void onRebind(Intent intent) {
        printf("service on rebind");
        super.onRebind(intent);
    }

    private void printf(String str) {
        Log.d(TAG, "###################------ " + str + "------");
    }

    private PaceServiceAIDL.Stub mBinder = new PaceServiceAIDL.Stub() {

        @Override
        public byte[] transmit(byte[] apdus) {
            Log.d(TAG, "transmit apdus:" + ByteUtil.toHexString(apdus));
            if (!mDeviceConnection.isConnected()) {
                return null;
            }
            return mSeInvoker.transmit(apdus);
        }

        @Override
        public int selectAid(String aid) {
            Log.d(TAG, "selectAid Thread:" + aid);
            if (!mDeviceConnection.isConnected()) {
                return -1;
            }
            return mSeInvoker.selectAID(aid);
        }

        @Override
        public int getDeviceInfo(PaceInfo info) {
            mDeviceConnection.getDeviceInfo(info);
            return 0;
        }

        @Override
        public int close() {
            if (!mDeviceConnection.isConnected()) {
                return -1;
            }
            return mSeInvoker.close();
        }

        @Override
        public int create(IPaceCallBack callback) {
            mDeviceConnection.addCallback(callback);
            return 0;
        }

        @Override
        public int destory(IPaceCallBack callback) {
            mDeviceConnection.rmCallback(callback);
            return 0;
        }

        @Override
        public int connect(String macId) {
            mDeviceConnection.connect(macId);
            return 0;
        }

        @Override
        public int disconnect() {
            mDeviceConnection.disConnect();
            return 0;
        }

        @Override
        public int scan() {
            mDeviceConnection.scan();
            return 0;
        }
    };

}
