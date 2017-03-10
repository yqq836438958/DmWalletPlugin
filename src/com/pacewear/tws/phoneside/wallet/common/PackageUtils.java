
package com.pacewear.tws.phoneside.wallet.common;

import android.content.Context;

import com.tencent.tws.framework.global.GlobalObj;

public class PackageUtils {
    public static String getAppMd5() {
        return "";
    }

    // 这里获取宿主app的包名
    public static String getHostAppName() {
        Context context = GlobalObj.g_appContext;
        return context.getPackageName();
    }
}
