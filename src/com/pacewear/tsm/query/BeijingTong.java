
package com.pacewear.tsm.query;

import android.text.TextUtils;

import com.pacewear.tsm.common.ByteUtil;

public class BeijingTong extends TsmApplet {

    @Override
    public String parse(String tag, String[] apdu) {
        String result = "";
        if (tag.equalsIgnoreCase("amount")) {
            result = "" + parseMoney(apdu[apdu.length - 1]);
        } else if (tag.equalsIgnoreCase("card_number")) {
            String tmp = apdu[apdu.length - 1];
            result = parseCardNum(tmp);
        }
        return result;
    }

    private String parseCardNum(String result) {
        if (TextUtils.isEmpty(result) || !result.endsWith("9000")) {
            return "null";
        }
        byte[] card = ByteUtil.toByteArray(result);
        byte[] bResult = new byte[8];
        System.arraycopy(card, 0, bResult, 0, 8);
        return ByteUtil.toHexString(bResult);
    }

    private int parseMoney(String money) {
        if (TextUtils.isEmpty(money) || !money.endsWith("9000")) {
            return -1;
        }
        byte[] tmp = ByteUtil.toByteArray(money);
        int len = tmp.length;
        byte[] bResult = new byte[len - 2];
        System.arraycopy(tmp, 0, bResult, 0, len - 2);
        return ByteUtil.toInt(bResult, 0, 4);
    }
}
