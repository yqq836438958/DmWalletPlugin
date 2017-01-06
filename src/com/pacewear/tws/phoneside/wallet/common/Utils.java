
package com.pacewear.tws.phoneside.wallet.common;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap.Config;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.text.TextUtils;

import com.pacewear.tws.phoneside.wallet.R;
import com.pacewear.tws.phoneside.wallet.WalletApp;
import com.pacewear.tws.phoneside.wallet.card.ICardInner.CONFIG;
import com.qq.taf.jce.JceInputStream;
import com.tencent.tws.framework.common.DevMgr;
import com.tencent.tws.framework.common.Device;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OptionalDataException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import TRom.PayConfig;

/**
 * @author baodingzhou
 */

public class Utils {

    private static final HandlerThread mWorkerHandlerThread = new HandlerThread("WalletWorker");

    private volatile static Looper mWorkerLooper = null;

    private volatile static Handler mWorkerHandler = null;

    private static final String COMMON_CACHE_FILE = "common_cache";

    private static final String TRAFFIC_CONFIG_CACHE_FILE = "trafficcard_config";
    /**
     * getWorkerlooper
     * 
     * @return
     */
    public static Looper getWorkerlooper() {
        if (mWorkerLooper == null) {
            synchronized (Utils.class) {
                if (mWorkerLooper == null) {
                    mWorkerHandlerThread.start();
                    mWorkerLooper = mWorkerHandlerThread.getLooper();
                }
            }
        }
        return mWorkerLooper;
    }

    /**
     * getWorkerHandler
     * 
     * @return
     */
    public static Handler getWorkerHandler() {
        if (mWorkerHandler == null) {
            synchronized (Utils.class) {
                if (mWorkerHandler == null) {
                    mWorkerHandler = new Handler(getWorkerlooper());
                }
            }
        }

        return mWorkerHandler;
    }

    /**
     * getJceInputStream
     * 
     * @param bs
     * @return
     */
    public static JceInputStream getJceInputStream(byte[] bs) {
        JceInputStream jceInputStream = new JceInputStream(bs);
        jceInputStream.setServerEncoding(Constants.UTF8);
        return jceInputStream;
    }

    /**
     * getDisplayBalance
     * 
     * @param balance
     * @return
     */
    public static String getDisplayBalance(String balance) {
        if (TextUtils.isEmpty(balance)) {
            return "";
        }
        long tmp = Long.parseLong(balance);
        return getDisplayBalance(tmp);
    }

    public static String getDisplayBalance(long balance) {
        DecimalFormat decimalFormat = new DecimalFormat("0.00");
        return decimalFormat.format(balance / 100f);
    }

    public static WalletCity getUserWalletCity() {
        String curCity = getUserCityCode();
        if (TextUtils.isEmpty(curCity)) {
            return getDefaultWalletCity();
        }
        curCity = curCity.substring(0, 4);
        WalletCity walletCity = new WalletCity();
        String[] citys = WalletApp.sGlobalCtx.getResources().getStringArray(R.array.wallet_city);
        for (String city : citys) {
            if (city != null && city.startsWith(curCity)) {
                String[] tmp = city.split("#");
                if (tmp != null && tmp.length > 2) {
                    walletCity.code = tmp[1];
                    walletCity.name = tmp[2];
                }
                break;
            }
        }
        if (TextUtils.isEmpty(walletCity.code)) {
            return getDefaultWalletCity();
        }
        return walletCity;
    }

    private static WalletCity getDefaultWalletCity() {
        WalletCity walletCity = new WalletCity();
        walletCity.code = Constants.WALLET_DEFAULT_CITYCODE_GZ;
        walletCity.name = "";
        return walletCity;
    }

    public static String getUserCityCode() {
        SharedPreferences oPreference = WalletApp.sGlobalCtx
                .getSharedPreferences("city", 0);
        return oPreference.getString("city_code", "");
    }

    public static List<String> getCityList() {
        String[] citys = WalletApp.sGlobalCtx.getResources().getStringArray(R.array.wallet_city);
        if (citys == null || citys.length <= 0) {
            return null;
        }
        return Arrays.asList(citys);
    }

    public static class WalletCity {
        public String code;
        public String name;
    }

    public static int compareDate(String srcDate, String destDate) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        Calendar c1 = Calendar.getInstance();
        Calendar c2 = Calendar.getInstance();
        try {
            c1.setTime(formatter.parse(srcDate));
            c2.setTime(formatter.parse(destDate));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        int result = c1.compareTo(c2);
        return result;
    }

    public static String getCurrentTime() {
        Date currentTime = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        String dateString = formatter.format(currentTime);
        return dateString;
    }

    public static String parseErrCodeDesc(int code, String aid) {
        String errDesc = null;
        String[] tmp = null;
        String[] errlist = null;
        if (code == 0) {
            return errDesc;
        }
        if (CONFIG.BEIJINGTONG.mAID.equals(aid)) {
            errlist = WalletApp.sGlobalCtx.getResources()
                    .getStringArray(R.array.beijingtong_cardinfo_errlist);
            for (String err : errlist) {
                if (!TextUtils.isEmpty(err) && err.startsWith(code + "")) {
                    tmp = err.split("#");
                    if (tmp != null && tmp.length > 1) {
                        errDesc = tmp[1];
                    }
                    break;
                }
            }
        }
        return errDesc;
    }

    public static String getCacheWhiteList() {
        SharedPreferences oPreference = WalletApp.sGlobalCtx
                .getSharedPreferences(TRAFFIC_CONFIG_CACHE_FILE, 0);
        return oPreference.getString("trafficcard_aid", "");
    }

    public static int getWhiteListSize() {
        SharedPreferences oPreference = WalletApp.sGlobalCtx
                .getSharedPreferences(TRAFFIC_CONFIG_CACHE_FILE, 0);
        return oPreference.getInt("trafficcard_count", -1);
    }

    public static void clearPayConfigs() {
        SharedPreferences oPreference = WalletApp.sGlobalCtx
                .getSharedPreferences(TRAFFIC_CONFIG_CACHE_FILE, 0);
        Editor oEditor = oPreference.edit();
        oEditor.clear();
        oEditor.commit();
    }

    public static void saveWhiteList2Cache(int count, String aid) {
        SharedPreferences oPreference = WalletApp.sGlobalCtx
                .getSharedPreferences(TRAFFIC_CONFIG_CACHE_FILE, 0);
        Editor oEditor = oPreference.edit();
        String val = TextUtils.isEmpty(aid) ? "empty" : aid;
        oEditor.putString("trafficcard_aid", val);
        oEditor.putInt("trafficcard_count", count);
        oEditor.commit();
    }

    public static void enableWalletMoudle(boolean enable) {
        SharedPreferences sharedPreferences = WalletApp.sGlobalCtx
                .getSharedPreferences(TRAFFIC_CONFIG_CACHE_FILE, 0);
        Editor editor = sharedPreferences.edit();
        editor.putBoolean("wallet_enable", enable);
        editor.commit();
    }

    public static boolean isWalletMoubleEnable() {
        SharedPreferences sharedPreferences = WalletApp.sGlobalCtx
                .getSharedPreferences(TRAFFIC_CONFIG_CACHE_FILE, 0);
        return sharedPreferences.getBoolean("wallet_enable", true);
    }

    public static boolean isAppInstalled(Context context, String packagename) {
        PackageInfo packageInfo;
        try {
            packageInfo = context.getPackageManager().getPackageInfo(packagename, 0);
        } catch (NameNotFoundException e) {
            packageInfo = null;
            e.printStackTrace();
        }
        return (packageInfo != null);
    }
    public static String getCacheCplc() {
        Device device = DevMgr.getInstance().connectedDev();
        if (device == null) {
            return "";
        }
        String key = device.devString();
        SharedPreferences sharedPreferences = WalletApp.sGlobalCtx.getSharedPreferences(
                COMMON_CACHE_FILE,
                0);
        return sharedPreferences.getString(key, "");
    }
    public static void saveCacheCplc(String cplc) {
        Device device = DevMgr.getInstance().connectedDev();
        if (device == null) {
            return;
        }
        String key = device.devString();
        SharedPreferences sharedPreferences = WalletApp.sGlobalCtx.getSharedPreferences(
                COMMON_CACHE_FILE,
                0);
        Editor editor = sharedPreferences.edit();
        editor.putString(key, cplc);
        editor.commit();
    }
    public static String getUserCacheCityCode() {
        SharedPreferences sharedPreferences = WalletApp.sGlobalCtx.getSharedPreferences(
                COMMON_CACHE_FILE,
                0);
        return sharedPreferences.getString("user_city_code", "");
    }

    public static void saveUserCitycode(String citycode) {
        SharedPreferences sharedPreferences = WalletApp.sGlobalCtx.getSharedPreferences(
                COMMON_CACHE_FILE,
                0);
        Editor editor = sharedPreferences.edit();
        editor.putString("user_city_code", citycode);
        editor.commit();
    }
}
