
package com.pacewear.tws.phoneside.wallet.ui2.fragment;

import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

public class CardBusinessLoadingView extends RelativeLayout {
    private ProgressBar mProgressBar = null;
    private ValueAnimator mValueAnimator = null;
    private static final int PERCENT_MAX_VALUE = 99;
    private static final long DEFALUT_TIME = 2 * 60 * 1000;

    public CardBusinessLoadingView(Context context, AttributeSet attrs, int defStyleAttr,
            int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        // TODO Auto-generated constructor stub
    }

    public CardBusinessLoadingView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        // TODO Auto-generated constructor stub
    }

    public CardBusinessLoadingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        // TODO Auto-generated constructor stub
    }

    public CardBusinessLoadingView(Context context) {
        super(context);
        // TODO Auto-generated constructor stub
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        if (!hasFocus) {
            mValueAnimator.pause();
        } else {
            mValueAnimator.resume();
        }
    }

    public final void clear() {
        mValueAnimator.cancel();
    }

    private void init(Context context) {
        // TODO init mProgressBar!!
        mValueAnimator = ValueAnimator.ofInt(0, PERCENT_MAX_VALUE);
        mValueAnimator.setDuration(DEFALUT_TIME);
        mValueAnimator.addUpdateListener(new AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator arg0) {
                // TODO Auto-generated method stub
                int progress = (Integer) arg0.getAnimatedValue();
                mProgressBar.setProgress(progress);

            }
        });
        mValueAnimator.start();
    }
}
