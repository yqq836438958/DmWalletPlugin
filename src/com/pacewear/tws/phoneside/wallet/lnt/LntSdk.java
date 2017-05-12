
package com.pacewear.tws.phoneside.wallet.lnt;

import android.content.Context;

import com.pacewear.httpserver.ServerHandler;
import com.pacewear.tws.phoneside.wallet.R;
import com.pacewear.tws.phoneside.wallet.common.DeviceUtil;
import com.pacewear.tws.phoneside.wallet.common.PackageUtils;
import com.pacewear.tws.phoneside.wallet.env.EnvManager;

public class LntSdk implements ILntSdk {
    private static volatile ILntSdk sInstance = null;

    public static ILntSdk getInstance() {
        if (sInstance == null) {
            synchronized (LntSdk.class) {
                if (sInstance == null) {
                    sInstance = new LntSdk();
                }
            }
        }
        return sInstance;
    }

    private LntSdk() {
        // TODO Auto-generated constructor stub
    }

    @Override
    public void clear() {
        LntLauncher.clear();
    }

    @Override
    public boolean charge(Context context, ILntInvokeCallback callback) {
        LntLauncher.cardTopup(newLntSdkCtx(context, callback));
        return true;
    }

    @Override
    public void complaint(Context context) {
        LntLauncher.complaint(newLntSdkCtx(context, null));
    }

    @Override
    public void complaintQuery(Context context) {
        LntLauncher.complaintQuery(newLntSdkCtx(context, null));
    }

    private LntSdkContext newLntSdkCtx(Context ctx, ILntInvokeCallback callback) {
        String usrId = EnvManager.getInstanceInner().getUserPhoneNum();
        String macAddr = DeviceUtil.getConnectedDeviceAddr();
        return LntSdkContextWrapper.newInstance(ctx,
                PackageUtils.getHostAppName(), macAddr,
                ctx.getString(R.string.wallet_default_cardcity), usrId,
                !ServerHandler.getInstance(ctx).isTestEnv(), callback);
    }

}
