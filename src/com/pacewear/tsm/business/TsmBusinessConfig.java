
package com.pacewear.tsm.business;

public class TsmBusinessConfig {
    public static String[] sAIDWhiteList = {
            "D0D1D2D3D4D50101",
            "535A542E57414C4C45542E454E56",
            "9156000014010001",
            "5943542E55534552",
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
