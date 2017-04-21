
package com.pacewear.tws.phoneside.wallet;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.util.Log;
import android.util.SparseArray;

import com.pacewear.tws.phoneside.wallet.sdkadapter.SdkAdapter;
import com.pacewear.tws.phoneside.wallet.watch.WatchBaseHandler;
import com.tencent.tws.framework.common.CommandHandler;
import com.tencent.tws.framework.common.MsgCmdDefine;
import com.tencent.tws.framework.common.MsgDispatcher;
import com.tencent.tws.framework.proxy.PluginCommandHandler;
import com.tencent.tws.sharelib.util.HostProxy;

import org.xutils.x;

import tws.component.log.TwsLog;

public class WalletApp extends Application {
    private static final String TAG = "rick_Pring:PluginTestApplication";
    private String packageName;
    private static Context sGlobalCtx = null;

    @Override
    public void onCreate() {
        super.onCreate();

        Context ctx = getApplicationContext();
        Log.d("xx", "" + ctx);

        if (isApplicationProcess()) {
            TwsLog.d(TAG, "api欺骗成功，让插件以为自己在主进程运行");
        }
        sGlobalCtx = ctx;
        registerCommandHandler();
        SdkAdapter.init();
        x.Ext.init(this); // TODO 后续由lntsdk去实现
    }
    public static Context getHostAppContext(){
        return HostProxy.getApplication().getApplicationContext();
    }
    public static Context getAppContext(){
        return sGlobalCtx;
    }

    private void registerCommandHandler() {
        MsgDispatcher.getInstance().appendPluginRecvMsg(build());
    }

    private SparseArray<CommandHandler> build() {
        SparseArray<CommandHandler> array = new SparseArray<CommandHandler>();
        array.put(MsgCmdDefine.CMD_NFC_WALLET_MSG_FROM_WATCH,
                new PluginCommandHandler(sGlobalCtx, WatchBaseHandler.class.getName()));
        return array;
    }

    private boolean isApplicationProcess() {
        ActivityManager mActivityManager = (ActivityManager) getSystemService(
                Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningAppProcessInfo appProcess : mActivityManager
                .getRunningAppProcesses()) {
            if (appProcess.pid == android.os.Process.myPid()) {
                if (appProcess.processName.equals(packageName)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        packageName = getPackageName();
    }
}
