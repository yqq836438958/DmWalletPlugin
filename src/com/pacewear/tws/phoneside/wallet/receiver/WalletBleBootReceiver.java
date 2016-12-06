
package com.pacewear.tws.phoneside.wallet.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.pacewear.tws.phoneside.wallet.common.Constants;
import com.pacewear.tws.phoneside.wallet.walletservice.IResult;
import com.pacewear.tws.phoneside.wallet.walletservice.PassData;
import com.tencent.tws.framework.common.DevMgr;
import com.tencent.tws.framework.common.Device;

import org.json.JSONException;
import org.json.JSONObject;

import qrom.component.log.QRomLog;

public class WalletBleBootReceiver extends BroadcastReceiver {
    public static final String TAG = "WalletBleBootReceiver";

    @Override
    public void onReceive(Context arg0, Intent arg1) {
        QRomLog.d(TAG, "on receive");
        if ("com.tencent.tws.wallet.blesrv".equals(arg1.getAction())) {
            int enable = arg1.getIntExtra(Constants.WALLET_BLESRV_ENABLE, 0);
            QRomLog.d(TAG, "on receive，isenable:" + enable);
            startBleService(enable);
        }
    }

    private void startBleService(int enable) {
        Device device = DevMgr.getInstance().connectedDev();
        if (device == null) {
            QRomLog.e(TAG, "device not connect, can not start blesrv");
            return;
        }
        try {
            JSONObject object = new JSONObject();
            object.put(Constants.WALLET_BLESRV_ENABLE, (enable == 1) ? "startble" : "stopble");
            PassData passdata = new PassData();
            passdata.putString(object.toString());
            passdata.invoke(new IResult() {

                @Override
                public void onResult(long seqID, int ret, String[] outputParams,
                        Integer[] resultCode,
                        byte[] bytes) {
                    QRomLog.d(TAG, "onResult:" + ret);
                }

                @Override
                public void onExecption(long seqID, int error) {
                    QRomLog.e(TAG, "onExecption:" + error);
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
