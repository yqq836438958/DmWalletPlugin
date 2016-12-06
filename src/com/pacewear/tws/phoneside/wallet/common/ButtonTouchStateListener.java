package com.pacewear.tws.phoneside.wallet.common;

import android.graphics.Color;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

public class ButtonTouchStateListener implements OnTouchListener {

    private static ButtonTouchStateListener sInstance = null;
    private float mLastWidgetOrignAlpha = -1f;

    private ButtonTouchStateListener() {
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        float orgin_alpha = (mLastWidgetOrignAlpha == -1f) ? v.getAlpha()
                : mLastWidgetOrignAlpha;
        float new_alpha = orgin_alpha / 2;
        switch (event.getAction()) {
        case MotionEvent.ACTION_DOWN:
            v.setAlpha(new_alpha);
            mLastWidgetOrignAlpha = orgin_alpha;
            break;
        case MotionEvent.ACTION_UP:
        case MotionEvent.ACTION_CANCEL:
            v.setAlpha(orgin_alpha);
            mLastWidgetOrignAlpha = -1;
            break;
        default:
            break;
        }
        return false;
    }

    public static ButtonTouchStateListener getInstance() {
        if (sInstance == null) {
            synchronized (ButtonTouchStateListener.class) {
                if (sInstance == null) {
                    sInstance = new ButtonTouchStateListener();
                }
            }
        }
        return sInstance;
    }
}