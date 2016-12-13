
package com.pacewear.tws.phoneside.wallet.common;

import android.graphics.Color;

import com.pacewear.tws.phoneside.wallet.WalletApp;
import com.tencent.tws.assistant.widget.TwsButton;

public class UIHelper {
    private static float DEFAULT_TWSBTN_SIZE = 16;
    private static String DEFAULT_TWSBTN_COLOR = "#ffffff";

    public static void setTwsButton(TwsButton button, int textResId) {
        setTwsButtonStyle(button, textResId, DEFAULT_TWSBTN_COLOR, DEFAULT_TWSBTN_SIZE);
    }

    public static void setTwsButton(TwsButton button, int textResId, String color) {
        setTwsButtonStyle(button, textResId, color, DEFAULT_TWSBTN_SIZE);
    }

    public static void setTwsButton(TwsButton button, int textResId, float txtSize) {
        setTwsButtonStyle(button, textResId, DEFAULT_TWSBTN_COLOR, txtSize);
    }

    public static void setTwsButtonStyle(TwsButton button, int textResId, String color,
            float textSize) {
        button.setText(WalletApp.sGlobalCtx.getString(textResId));
        button.setTextColor(Color.parseColor(color));
        button.setTextSize(textSize);
    }
}
