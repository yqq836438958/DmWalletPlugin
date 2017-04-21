
package com.pacewear.tws.wallet.service;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.util.Log;

import com.pacewear.tws.phoneside.wallet.common.DeviceUtil;
import com.tencent.tws.api.BroadcastDef;
import com.tencent.tws.framework.common.ConnectionStrategy;
import com.tencent.tws.framework.common.ConstantUtils;
import com.tencent.tws.framework.common.DevMgr;
import com.tencent.tws.phoneside.utils.WatchDeviceUtil;
import com.tencent.tws.phoneside.utils.WatchDeviceUtil.OnScanCompletedListener;
import com.tws.plugin.aidl.IPaceCallBack;
import com.tws.plugin.aidl.PaceInfo;

import java.util.List;

import qrom.component.log.QRomLog;

public class PaceDeviceConnect implements IDeviceConnect {
    public static final int PACE_SCAN_TIMEOUT = 5000;
    protected static final String TAG = "PaecDeviceConnect";
    private RemoteCallbackList<IPaceCallBack> mCallbacks = new RemoteCallbackList<IPaceCallBack>();
    private Context mContext = null;
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent == null || intent.getAction() == null) {
                return;
            }
            String action = intent.getAction();
            QRomLog.d(TAG, "monitorWatchConnection onReceive action:" + action);

            if (action.equalsIgnoreCase(BroadcastDef.DEVICE_CONNECTED)) {
                QRomLog.d(TAG, "watch connct onreceive  :DEVICE_CONNECTED");
                dispatchConnectCallbacks(true, DeviceUtil.getConnectedDeviceAddr());
            } else if (action.equalsIgnoreCase(BroadcastDef.DEVICE_ACTIVE_DISCONNECTED)
                    || action.equalsIgnoreCase(BroadcastDef.DEVICE_PASSIVE_DISCONNECTED)) {
                QRomLog.d(TAG, "watch connct onreceive  :" + action);
                dispatchConnectCallbacks(false, "");
            }
        }
    };

    public PaceDeviceConnect(Context context) {
        mContext = context;
    }

    public void addCallback(IPaceCallBack callback) {
        if (callback != null) {
            mCallbacks.register(callback);
        }
    }

    public void rmCallback(IPaceCallBack callback) {
        if (callback != null) {
            mCallbacks.unregister(callback);
        }
    }

    @Override
    public void connect(String mac) {
        BluetoothDevice bluetoothDevice = DeviceUtil.getConnectedDeviceInfo(true);
        QRomLog.d(TAG, "connect from client:" + mac);
        if (bluetoothDevice != null && !mac.equalsIgnoreCase(bluetoothDevice.getAddress())) {
            QRomLog.e(TAG,
                    "device has connected with another device:" + bluetoothDevice.getAddress());
            dispatchConnectCallbacks(false, mac);
            return;
        }
        int ret = ConnectionStrategy.getInstance().connectRemoteDeviceIfDisconnect();
        switch (ret) {
            case ConstantUtils.STATE_CONNECTED:
                QRomLog.d(TAG, "connectRemoteDeviceIfDisconnect :STATE_CONNECTED");
                dispatchConnectCallbacks(true, mac);
                break;
            case ConstantUtils.STATE_CONNECTING:
                QRomLog.d(TAG, "connectRemoteDeviceIfDisconnect :STATE_CONNECTING");
                break;
            case ConstantUtils.STATE_NO_PAIRED_DEVICE:
            case ConstantUtils.STATE_BT_DISABLE:
            case ConstantUtils.STATE_ACCOUNT_EXPIRE:
            default:
                QRomLog.d(TAG, "connectRemoteDeviceIfDisconnect :" + ret);
                dispatchConnectCallbacks(false, mac);
                break;
        }
    }

    @Override
    public void disConnect() {
        DevMgr.getInstance().asyncDisconnectDev();
    }

    @Override
    public void scan() {
        if (BluetoothAdapter.getDefaultAdapter().isEnabled()) {
            WatchDeviceUtil.scanDeviceList(mContext, PACE_SCAN_TIMEOUT,
                    new OnScanCompletedListener() {

                        @Override
                        public void onScanCompleted(List<BluetoothDevice> devices) {
                            Log.d(TAG, "onScanCompleted");
                            int length = (devices != null) ? devices.size() : 0;
                            PaceInfo[] infos = new PaceInfo[length];
                            int cnt = 0;
                            for (BluetoothDevice device : devices) {
                                Log.d(TAG, "device.name=" + device.getName() + ",addr:"
                                        + device.getAddress());
                                infos[cnt] = new PaceInfo();
                                infos[cnt].setDevName(device.getName());
                                infos[cnt].setMacAddr(device.getAddress());
                                if (device.getAddress()
                                        .equalsIgnoreCase(DeviceUtil.getConnectedDeviceAddr())) {
                                    infos[cnt].setConnect(true);
                                } else {
                                    infos[cnt].setConnect(false);
                                }
                                cnt++;
                            }
                            dispatchScanCallback(infos);
                        }
                    });
        } else {
            Log.d(TAG, "please open bluetooth first");
        }

    }

    private void dispatchScanCallback(PaceInfo[] list) {
        final int N = mCallbacks.beginBroadcast();
        for (int i = 0; i < N; i++) {
            mCallbacks.getBroadcastItem(i).onScanResult(list);
        }
        mCallbacks.finishBroadcast();
    }

    private void dispatchConnectCallbacks(boolean connect, String mac) {
        final int N = mCallbacks.beginBroadcast();
        for (int i = 0; i < N; i++) {
            mCallbacks.getBroadcastItem(i).onConnectResult(connect, mac);
        }
        mCallbacks.finishBroadcast();
    }

    @Override
    public void getDeviceInfo(PaceInfo info) {
        info.setConnect(ConnectionStrategy.getInstance().isConnected());
        info.setMacAddr(DeviceUtil.getConnectedDeviceAddr());
        info.setDevName(DeviceUtil.getConnectedDeviceName());
    }

    @Override
    public void init() {
        QRomLog.d(TAG, "monitorWatchConnection");
        IntentFilter intent = new IntentFilter();
        intent.addAction(BroadcastDef.DEVICE_CONNECTED);
        intent.addAction(BroadcastDef.DEVICE_ACTIVE_DISCONNECTED);
        intent.addAction(BroadcastDef.DEVICE_PASSIVE_DISCONNECTED);
        mContext.registerReceiver(mReceiver, intent);
    }

    @Override
    public void deinit() {
        mContext.unregisterReceiver(mReceiver);
    }

    @Override
    public boolean isConnected() {
        return ConnectionStrategy.getInstance().isConnected();
    }
}
