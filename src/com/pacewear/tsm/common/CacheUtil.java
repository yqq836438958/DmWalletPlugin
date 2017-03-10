
package com.pacewear.tsm.common;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.text.TextUtils;

import com.pacewear.common.utils.CacheUtils;
import com.pacewear.tsm.TsmService;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;

public class CacheUtil {
    private static final String SHARED_FILE = "tsm_file";
    private static final String KEY_CPLC = "key_cplc";
    private static final String KEY_MAIN_ISD = "key_isd_key";
    private static final String KEY_REMOTE_STAT = "key_remote_stat_";

    public static String getCacheISD() {
        prepareTsmSharePref();
        return CacheUtils.get(KEY_MAIN_ISD, "");
    }

    public static void saveRemoteStatus(String cplc, Object obj) {
        prepareTsmSharePref();
        CacheUtils.save(KEY_REMOTE_STAT + cplc, obj);
    }

    public static Object getRemoteStatus(String cplc) {
        prepareTsmSharePref();
        return CacheUtils.get(KEY_REMOTE_STAT + cplc);
    }

    public static void saveCacheISD(String isd) {
        prepareTsmSharePref();
        CacheUtils.save(KEY_MAIN_ISD, isd);
    }

    public static String getCacheCPLC() {
        prepareTsmSharePref();
        return CacheUtils.get(KEY_CPLC, "");
    }

    public static void saveCacheCPLC(String cplc) {
        prepareTsmSharePref();
        CacheUtils.save(KEY_CPLC, cplc);
    }

    public static void saveCardList(String cplc, String cardlist) {
        prepareTsmSharePref();
        CacheUtils.save(cplc, cardlist);
    }

    public static String getCardList(String cplc) {
        prepareTsmSharePref();
        return CacheUtils.get(cplc, "");
    }

    private static void prepareTsmSharePref() {
        CacheUtils.prepare(SHARED_FILE);
    }
}
