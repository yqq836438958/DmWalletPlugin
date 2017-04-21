
package com.pacewear.tws.phoneside.wallet.common;

import android.content.Context;

import com.pacewear.tws.phoneside.wallet.WalletApp;

public class PackageUtils {
    public static String getAppMd5() {
        return "";
    }

    // 这里获取宿主app的包名
    public static String getHostAppName() {
        Context context = WalletApp.getHostAppContext();
        return context.getPackageName();
    }
}
