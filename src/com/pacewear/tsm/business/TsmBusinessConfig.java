
package com.pacewear.tsm.business;

public class TsmBusinessConfig {
    public static String[] sAIDWhiteList = {
            "D0D1D2D3D4D50101"
    };

    public static boolean isInWhiteList(String aid) {
        boolean bFound = false;
        for (String tmp : sAIDWhiteList) {
            if (tmp.equalsIgnoreCase(aid)) {
                bFound = true;
                break;
            }
        }
        return bFound;
    }
}
