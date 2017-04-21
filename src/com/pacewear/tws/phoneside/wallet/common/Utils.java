
package com.pacewear.tws.phoneside.wallet.common;

import android.content.SharedPreferences;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.text.TextUtils;

import com.pacewear.common.utils.CacheUtils;
import com.pacewear.tws.phoneside.wallet.R;
import com.pacewear.tws.phoneside.wallet.WalletApp;
import com.pacewear.tws.phoneside.wallet.card.ICardInner.CONFIG;
import com.qq.taf.jce.JceInputStream;
import com.tencent.tws.framework.common.DevMgr;
import com.tencent.tws.framework.common.Device;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * @author baodingzhou
 */

public class Utils {

    private static final HandlerThread mWorkerHandlerThread = new HandlerThread("WalletWorker");

    private volatile static Looper mWorkerLooper = null;

    private volatile static Handler mWorkerHandler = null;
    private static final String TRAFFIC_CONFIG_CACHE_FILE = "trafficcard_config";
    private static final String TRAFFIC_AID = "trafficcard_aid";
    private static final String TRAFFIC_COUNT = "trafficcard_count";
    private static final String USER_CITY_CODE = "user_city_code";
    private static final String WALLET_ENABLE = "wallet_module_enable";
    private static final String PREFIX_KEY_CPLC = "key_cplc_";
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
        String[] citys = WalletApp.getAppContext().getResources().getStringArray(R.array.wallet_city);
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
        SharedPreferences oPreference = WalletApp.getAppContext()
                .getSharedPreferences("city", 0);
        return oPreference.getString("city_code", "");
    }

    public static List<String> getCityList() {
        String[] citys = WalletApp.getAppContext().getResources().getStringArray(R.array.wallet_city);
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
            errlist = WalletApp.getAppContext().getResources()
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
        CacheUtils.prepare(TRAFFIC_CONFIG_CACHE_FILE);
        return CacheUtils.get(TRAFFIC_AID, "");
    }

    public static int getWhiteListSize() {
        CacheUtils.prepare(TRAFFIC_CONFIG_CACHE_FILE);
        return CacheUtils.get(TRAFFIC_COUNT, -1);
    }

    public static void clearPayConfigs() {
        CacheUtils.prepare(TRAFFIC_CONFIG_CACHE_FILE);
        CacheUtils.save(TRAFFIC_AID, "");
        CacheUtils.save(TRAFFIC_COUNT, -1);
    }

    public static void saveWhiteList2Cache(int count, String aid) {
        String val = TextUtils.isEmpty(aid) ? "empty" : aid;
        CacheUtils.prepare(TRAFFIC_CONFIG_CACHE_FILE);
        CacheUtils.save(TRAFFIC_AID, val);
        CacheUtils.save(TRAFFIC_COUNT, count);
    }

    public static void enableWalletMoudle(boolean enable) {
        CacheUtils.prepare(TRAFFIC_CONFIG_CACHE_FILE);
        CacheUtils.save(WALLET_ENABLE, enable);
    }

    public static boolean isWalletMoubleEnable() {
        CacheUtils.prepare(TRAFFIC_CONFIG_CACHE_FILE);
        return CacheUtils.getBoolean(WALLET_ENABLE, true);
    }

    public static String getCacheCplc() {
        Device device = DevMgr.getInstance().connectedDev();
        if (device == null) {
            return "";
        }
        CacheUtils.prepare(TRAFFIC_CONFIG_CACHE_FILE);
        return CacheUtils.get(device.devString(), "");
    }

    public static void saveCacheCplc(String cplc) {
        Device device = DevMgr.getInstance().connectedDev();
        if (device == null) {
            return;
        }
        CacheUtils.prepare(TRAFFIC_CONFIG_CACHE_FILE);
        CacheUtils.save(device.devString(), cplc);
    }

    public static void saveCacheCardList(String list) {
//        String cplc = getCacheCplc();
//        if (TextUtils.isEmpty(cplc)) {
//            return;
//        }
//        CacheUtils.prepare(TRAFFIC_CONFIG_CACHE_FILE);
//        CacheUtils.save(PREFIX_KEY_CPLC+cplc, list);
    }

    public static String getCacheCardList() {
//        String cplc = getCacheCplc();
//        if (TextUtils.isEmpty(cplc)) {
//            return "";
//        }
//        CacheUtils.prepare(TRAFFIC_CONFIG_CACHE_FILE);
//        return CacheUtils.get(PREFIX_KEY_CPLC+cplc, "");
        return "";
    }

    public static String getUserCacheCityCode() {
        CacheUtils.prepare(TRAFFIC_CONFIG_CACHE_FILE);
        return CacheUtils.get(USER_CITY_CODE, "");
    }

    public static void saveUserCitycode(String citycode) {
        CacheUtils.prepare(TRAFFIC_CONFIG_CACHE_FILE);
        CacheUtils.save(USER_CITY_CODE, citycode);
    }
}
