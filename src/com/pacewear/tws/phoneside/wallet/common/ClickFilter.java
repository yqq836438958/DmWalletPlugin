
package com.pacewear.tws.phoneside.wallet.common;

public class ClickFilter {
    private static final long CLICK_INTERVAL = 500L;
    private static long lastClickTime = 0L;

    /**
     * 重置点击过滤器, 在每次用代码模拟View点击事件的时候, 要先reset一下, 以防被过滤掉
     */
    public static void resetMultiClick() {
        lastClickTime = 0L;
    }

    public static boolean isMultiClick() {
        long time = System.currentTimeMillis();
        if ((time - lastClickTime) > CLICK_INTERVAL) {
            lastClickTime = time;
            return false;
        }
        lastClickTime = time;
        return true;
    }
}
