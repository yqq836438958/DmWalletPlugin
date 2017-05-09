
package com.pacewear.tws.phoneside.wallet.ui2.widget;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.pacewear.tws.phoneside.wallet.R;
import com.pacewear.tws.phoneside.wallet.common.DisplayUtil;
import com.pacewear.tws.phoneside.wallet.common.UIHelper;

public class TimerButton extends TextView implements OnClickListener {
    private long lenght = 60 * 1000;
    private String textafter = null;
    private String textbefore = null;
    private String textInit = null;
    public static final String LEFT_TIME = "lefttime";
    public static final String CURTIME = "curtime";
    public static Map<String, Long> mTimeMap = new HashMap<String, Long>();
    private OnClickListener mOnclickListener = null;
    private Timer mTimer = null;
    private TimerTask mTimerTask = null;
    private long mLeftTime = 0;

    public TimerButton(Context context) {
        this(context, null);
    }

    public TimerButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        textInit = context.getString(R.string.wallet_get_verify);
        textbefore = context.getString(R.string.wallet_reget_verify_reset);
        textafter = context.getString(R.string.wallet_reget_verify);
        getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
        this.setGravity(Gravity.CENTER);
        this.setTextSize(14);
        setText(textInit);
        setOnClickListener(this);
    }

    @SuppressLint("HandlerLeak")
    Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            TimerButton.this.setText(getAfterBtnText(mLeftTime / 1000));
            mLeftTime -= 1000;
            if (mLeftTime < 0) {
                setButtonEnableInternal(true);
                TimerButton.this.setText(textbefore);
                clearTimer();
            }
        }
    };

    private void initTimer() {
        mLeftTime = lenght;
        mTimer = new Timer();
        mTimerTask = new TimerTask() {

            @Override
            public void run() {
                mHandler.sendEmptyMessage(0x01);
            }
        };
    }

    private void clearTimer() {
        if (mTimerTask != null) {
            mTimerTask.cancel();
            mTimerTask = null;
        }
        if (mTimer != null)
            mTimer.cancel();
        mTimer = null;
    }

    @Override
    public void setOnClickListener(OnClickListener l) {
        if (l instanceof TimerButton) {
            super.setOnClickListener(l);
        } else
            this.mOnclickListener = l;
    }

    @Override
    public void onClick(View v) {
        if (mOnclickListener != null) {
            mOnclickListener.onClick(v);
        }
    }

    public void onPerformClick() {
        initTimer();
        this.setText(getAfterBtnText(mLeftTime / 1000));
        mTimer.schedule(mTimerTask, 0, 1000);
        setButtonEnableInternal(false);
    }

    public void onDestroy() {
        mTimeMap.put(LEFT_TIME, mLeftTime);
        mTimeMap.put(CURTIME, System.currentTimeMillis());
        clearTimer();
    }

    public void onCreate(Bundle bundle) {
        if (mTimeMap.size() <= 0)
            return;
        long time = System.currentTimeMillis() - mTimeMap.get(CURTIME)
                - mTimeMap.get(LEFT_TIME);
        mTimeMap.clear();
        if (time > 0) {
            return;
        } else {
            initTimer();
            this.mLeftTime = Math.abs(time);
            mTimer.schedule(mTimerTask, 0, 1000);
            this.setText(getAfterBtnText(0));
            setButtonEnableInternal(false);
        }
    }

    public void setButtonEnable(boolean enable) {
        boolean idleState = (mLeftTime <= 0);
        setButtonEnableInternal(enable && idleState);
    }

    private void setButtonEnableInternal(boolean enable) {
        super.setEnabled(enable);
        // int colorGreen = getResources().getColor(R.color.wallet_green);
        // int colorLightGray = getResources().getColor(R.color.wallet_light_gray);
        // setTextColor(enable ? colorGreen : colorLightGray);
        setTextColor(enable ? Color.parseColor("#FF00BB00") : Color.parseColor("#33000000"));
    }

    private String getAfterBtnText(long ms) {
        return textafter.replace("#", ms + "");
    }
}
