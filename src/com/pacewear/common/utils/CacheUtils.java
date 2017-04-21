
package com.pacewear.common.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.text.TextUtils;

import com.pacewear.tsm.common.ByteUtil;
import com.pacewear.tws.phoneside.wallet.WalletApp;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;

public class CacheUtils {
    private static String sShareFile = "";

    public static void prepare(String fileName) {
        sShareFile = fileName;
    }

    public static void save(String key, String val) {
        Editor editor = getSharePref().edit();
        editor.putString(key, val);
        editor.commit();
    }

    public static void save(String key, boolean val) {
        Editor editor = getSharePref().edit();
        editor.putBoolean(key, val);
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

    public static boolean getBoolean(String key, boolean defVal) {
        return getSharePref().getBoolean(key, defVal);
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
        Context context = WalletApp.getAppContext();
        SharedPreferences sharedPreferences = context.getSharedPreferences(sShareFile,
                Context.MODE_PRIVATE);
        return sharedPreferences;
    }
}
