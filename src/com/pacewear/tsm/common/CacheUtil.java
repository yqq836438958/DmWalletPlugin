
package com.pacewear.tsm.common;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.text.TextUtils;

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

    public static String getCacheISD() {
        return get(KEY_MAIN_ISD, "");
    }

    public void saveCacheISD(String isd) {
        save(KEY_MAIN_ISD, isd);
    }

    public static String getCacheCPLC() {
        return get(KEY_CPLC, "");
    }

    public static void saveCacheCPLC(String cplc) {
        save(KEY_CPLC, cplc);
    }

    public static void clearAll() {
        Editor editor = getSharePref().edit();
        editor.clear();
        editor.commit();
    }

    public static void saveCardList(String cplc, String cardlist) {
        save(cplc, cardlist);
    }

    public static String getCardList(String cplc) {
        return get(cplc, "");
    }

    public static void save(String key, String val) {
        Editor editor = getSharePref().edit();
        editor.putString(key, val);
        editor.commit();
    }

    public static void save(String key, Object obj) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            ObjectOutputStream os = new ObjectOutputStream(bos);
            os.writeObject(obj);
            String objStr = ByteUtil.toHexString(bos.toByteArray());
            save(key, objStr);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void save(String key, int val) {
        Editor editor = getSharePref().edit();
        editor.putInt(key, val);
        editor.commit();
    }

    public static String get(String key, String defVal) {
        return getSharePref().getString(key, defVal);
    }

    public static int get(String key, int defVal) {
        return getSharePref().getInt(key, defVal);
    }

    public static Object get(String key) {
        String val = get(key, "");
        if (TextUtils.isEmpty(val)) {
            return null;
        }
        byte[] stringToBytes = ByteUtil.toByteArray(val);
        ByteArrayInputStream bis = new ByteArrayInputStream(stringToBytes);
        ObjectInputStream is;
        Object readObject = null;
        try {
            is = new ObjectInputStream(bis);
            readObject = is.readObject();
        } catch (StreamCorruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return readObject;
    }

    private static SharedPreferences getSharePref() {
        Context context = TsmService.getInstance().getContext();
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_FILE,
                Context.MODE_PRIVATE);
        return sharedPreferences;
    }
}
