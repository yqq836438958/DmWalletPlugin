
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
import java.io.OptionalDataException;

public class ByteUtil {
    /**
     * 16进制的字符串表示转成字节数组
     *
     * @param hexString 16进制格式的字符串
     * @return 转换后的字节数组
     **/
    public static byte[] toByteArray(String hexString) {
        if (TextUtils.isEmpty(hexString))
            throw new IllegalArgumentException("this hexString must not be empty");

        hexString = hexString.toLowerCase();
        final byte[] byteArray = new byte[hexString.length() / 2];
        int k = 0;
        for (int i = 0; i < byteArray.length; i++) {// 因为是16进制，最多只会占用4位，转换成字节需要两个16进制的字符，高位在先
            byte high = (byte) (Character.digit(hexString.charAt(k), 16) & 0xff);
            byte low = (byte) (Character.digit(hexString.charAt(k + 1), 16) & 0xff);
            byteArray[i] = (byte) (high << 4 | low);
            k += 2;
        }
        return byteArray;
    }

    /**
     * 字节数组转成16进制表示格式的字符串
     *
     * @param byteArray 要转换的字节数组
     * @return 16进制表示格式的字符串
     **/
    public static String toHexString(byte[] byteArray) {
        if (byteArray == null || byteArray.length < 1) {
            return "";
        }
        final StringBuilder hexString = new StringBuilder();
        for (int i = 0; i < byteArray.length; i++) {
            if ((byteArray[i] & 0xff) < 0x10)// 0~F前面不零
                hexString.append("0");
            hexString.append(Integer.toHexString(0xFF & byteArray[i]));
        }
        return hexString.toString().toLowerCase();
    }

    public static boolean isResponseSuccess(String rsp) {
        if (TextUtils.isEmpty(rsp)) {
            return false;
        }
        return rsp.endsWith("9000") || rsp.endsWith("6310");
    }

    private static String toHexUtil(int n) {
        String rt = "";
        switch (n) {
            case 10:
                rt += "A";
                break;
            case 11:
                rt += "B";
                break;
            case 12:
                rt += "C";
                break;
            case 13:
                rt += "D";
                break;
            case 14:
                rt += "E";
                break;
            case 15:
                rt += "F";
                break;
            default:
                rt += n;
        }
        return rt;
    }

    public static String toHex(int n) {
        StringBuilder sb = new StringBuilder();
        if (n / 16 == 0) {
            return toHexUtil(n);
        } else {
            String t = toHex(n / 16);
            int nn = n % 16;
            sb.append(t).append(toHexUtil(nn));
        }
        return sb.toString();
    }

    public static String parseAscii(String str) {
        StringBuilder sb = new StringBuilder();
        byte[] bs = str.getBytes();
        for (int i = 0; i < bs.length; i++)
            sb.append(toHex(bs[i]));
        return sb.toString();
    }

    //
    // public static String getHexString(String byteStr) {
    // byte[] byteArray = null;
    // try {
    // byteArray = byteStr.getBytes("ISO-8859-1");
    // } catch (UnsupportedEncodingException e) {
    // e.printStackTrace();
    // return "";
    // }
    // return toHexString(byteArray);
    // }
    //
    // public static ArrayList<String> getHexStrings(ArrayList<String> sourceList) {
    // if (sourceList == null || sourceList.size() < 1) {
    // return null;
    // }
    // ArrayList<String> list = new ArrayList<String>();
    // for (String str : sourceList) {
    // list.add(getHexString(str));
    // }
    // return list;
    // }
    public static void saveClassObject2Cache(String aid, Object object) {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream os = new ObjectOutputStream(bos);
            os.writeObject(object);
            String objStr = ByteUtil.toHexString(bos.toByteArray());
            Context context = TsmService.getInstance().getContext();
            SharedPreferences oPreference = context.getSharedPreferences("class_cache", 0);
            Editor oEditor = oPreference.edit();
            oEditor.putString(aid, objStr);
            oEditor.commit();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Object getClassObjectFormCache(String aid) {
        Context context = TsmService.getInstance().getContext();
        SharedPreferences oPreference = context
                .getSharedPreferences("class_cache", 0);
        String result = oPreference.getString(aid, "");
        Object readObject = null;
        if (TextUtils.isEmpty(result)) {
            return null;
        }
        byte[] stringToBytes = ByteUtil.toByteArray(result);
        ByteArrayInputStream bis = new ByteArrayInputStream(stringToBytes);
        // 返回反序列化得到的对象
        try {
            ObjectInputStream is = new ObjectInputStream(bis);
            readObject = is.readObject();
        } catch (OptionalDataException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return readObject;
    }
}
