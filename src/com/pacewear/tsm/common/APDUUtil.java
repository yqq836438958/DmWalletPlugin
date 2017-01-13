
package com.pacewear.tsm.common;

import android.text.TextUtils;

import org.json.JSONArray;

import java.util.ArrayList;

public class APDUUtil {
    // public String get
    public static final String KEY_INSTANCEID = "instance_id";
    public static final String TAG_AMOUNT = "balance";
    public static final String TAG_INVALID_DATE = "invalid_date";
    public static final String TAG_START_DATE = "start_date";
    public static final String TAG_CARD_NAME = "card_num";
    public static final String TAG_CARD_ID = "card_id";
    private static JSONArray mApduArr = new JSONArray();
    public static final String ACTIVE_APP_APDU = "80F00101#4F@(aid)";
    public static final String DISACTIVE_APP_APDU = "80F00100#4F@(aid)";
    public static final String LISTSTATE_APDU = "80F24000#4F@(aid)";

    public static String listCRSApp() {
        return getCRSAppStat("00");
    }

    public static int parseAppStat(String apdu, String aid) {
        if (TextUtils.isEmpty(apdu) || TextUtils.isEmpty(aid)) {
            return -1;
        }
        byte[] bsApdu = ByteUtil.toByteArray(apdu);
        int aidIndex = apdu.lastIndexOf(aid);
        aidIndex += aid.length() - 1 + 10;
        int offset = aidIndex / 2;
        byte result = bsApdu[offset];
        return (int) result;
    }

    public static String getCRSAppStat(String aid) {
        return wrapApduTemplete(LISTSTATE_APDU, aid);
    }

    public static String activeApp(String aid) {
        return wrapApduTemplete(ACTIVE_APP_APDU, aid);
    }

    public static String disactiveApp(String aid) {
        return wrapApduTemplete(DISACTIVE_APP_APDU, aid);
    }

    private static String wrapApduTemplete(String tmplete, String aid) {
        int aidLen = aid.length() / 2;
        String aidSegment = "00".equals(aid) ? "" : ByteUtil.toHex(aidLen);
        int datLen = "00".equals(aid) ? 2 : aid.length() / 2 + 2;
        return tmplete.replace("#", ByteUtil.toHex(datLen)).replace("@", aidSegment)
                .replace("(aid)", aid);
    }

    public static void updateApduList(JSONArray array) {
        mApduArr = array;
    }

    public static ArrayList<String> getAPDU(String instanceId, String tag) {
        return null;
    }
}
