
package com.pacewear.tws.phoneside.wallet.common;

public class WalletRunEnv {
    public static int FROM_PACEWEAR = 0;
    public static int FROM_BEIJING = 1;
    private static int sVisitChannel = FROM_PACEWEAR;

    public static void setVisitChannel(int val) {
        sVisitChannel = val;
    }

    public static void resetVisitChannel() {
        sVisitChannel = FROM_PACEWEAR;
    }

    public static int getVisitChannel() {
        return sVisitChannel;
    }

    public static int getDefaultChannel() {
        return FROM_PACEWEAR;
    }
}
