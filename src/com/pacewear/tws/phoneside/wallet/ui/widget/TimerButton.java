
package com.pacewear.tws.phoneside.wallet.ui.widget;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;

import com.example.plugindemo.R;
import com.tencent.tws.assistant.widget.TwsButton;

public class TimerButton extends TwsButton implements OnClickListener {
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
    private long mLeftTime;

    public TimerButton(Context context) {
        this(context, null);
    }

    public TimerButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        textInit = context.getString(R.string.wallet_get_verify);
        textbefore = context.getString(R.string.wallet_reget_verify_reset);
        textafter = context.getString(R.string.wallet_reget_verify);
        // setTextNoPadding();
        setText(textInit);
        setOnClickListener(this);
    }

    @SuppressLint("HandlerLeak")
    Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            TimerButton.this.setText(getAfterBtnText(mLeftTime / 1000));
            mLeftTime -= 1000;
            if (mLeftTime < 0) {
                TimerButton.this.setEnabled(true);
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
        this.setEnabled(false);
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
            this.setEnabled(false);
        }
    }

    private String getAfterBtnText(long ms) {
        return textafter.replace("#", ms + "");
    }
}
