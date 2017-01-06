
package com.pacewear.tsm.common;

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

    public static String activeApp(String aid) {
        return getActOrDisActAppString(ACTIVE_APP_APDU, aid);
    }

    public static String disactiveApp(String aid) {
        return getActOrDisActAppString(DISACTIVE_APP_APDU, aid);
    }

    private static String getActOrDisActAppString(String tmplete, String aid) {
        int aidLen = aid.length() / 2;
        int datLen = aid.length() / 2 + 2;
        return tmplete.replace("#", ByteUtil.toHex(datLen)).replace("@", ByteUtil.toHex(aidLen))
                .replace("(aid)", aid);
    }

    public static void updateApduList(JSONArray array) {
        mApduArr = array;
    }

    public static ArrayList<String> getAPDU(String instanceId, String tag) {
        return null;
    }
}
