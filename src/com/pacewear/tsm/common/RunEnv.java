
package com.pacewear.tsm.common;

public class RunEnv {
    public static final int PLATFORM_DM = 0;
    public static final int PLATFORM_WATCH = 1;
    public static final int PLATFORM_3RD = 2;
    private static int mPlatformEnv = PLATFORM_DM;
    public static final String TAG = "RunEnv";

    public static void init() {

    }

    public static int getPlatform() {
        return mPlatformEnv;
    }

}
