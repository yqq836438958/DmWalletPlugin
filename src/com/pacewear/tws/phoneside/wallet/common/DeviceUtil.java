
package com.pacewear.tws.phoneside.wallet.common;

import android.bluetooth.BluetoothDevice;

import com.tencent.tws.framework.common.BluetoothDeviceWraper;
import com.tencent.tws.framework.common.DevMgr;
import com.tencent.tws.framework.common.Device;

public class DeviceUtil {
    public static String getConnectedDeviceName() {
        BluetoothDevice bluetoothDevice = getConnectedDeviceInfo(false);
        return bluetoothDevice != null ? bluetoothDevice.getName() : "";
    }

    public static String getConnectedDeviceAddr() {
        BluetoothDevice bluetoothDevice = getConnectedDeviceInfo(false);
        return bluetoothDevice != null ? bluetoothDevice.getAddress() : "";
    }

    public static BluetoothDevice getConnectedDeviceInfo(boolean isLastDev) {
        Device dev = isLastDev ? DevMgr.getInstance().lastConnectedDev()
                : DevMgr.getInstance().connectedDev();
        if (dev == null) {
            return null;
        }
        BluetoothDevice bluetoothDevice = (BluetoothDevice) ((BluetoothDeviceWraper) dev)
                .deviceObj();
        return bluetoothDevice;
    }
}
