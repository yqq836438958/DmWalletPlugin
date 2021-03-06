
package com.pacewear.tws.phoneside.wallet.ui2.widget;

import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.pacewear.tws.phoneside.wallet.R;

public class CardBusinessLoadingView extends RelativeLayout {
    private ProgressBar mProgressBar = null;
    private ValueAnimator mValueAnimator = null;
    private static final int PERCENT_MAX_VALUE = 99;
    private static final long DEFALUT_TIME = 2 * 60 * 1000;

    public CardBusinessLoadingView(Context context, AttributeSet attrs, int defStyleAttr,
            int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    public CardBusinessLoadingView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, null, -1, -1);
    }

    public CardBusinessLoadingView(Context context, AttributeSet attrs) {
        this(context, null, -1);
    }

    public CardBusinessLoadingView(Context context) {
        this(context, null);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        // if (!hasFocus) {
        // mValueAnimator.pause();
        // } else {
        // mValueAnimator.resume();
        // }
    }

    public final void clear() {
        mValueAnimator.cancel();
    }

    public void attach(long duration) {

    }

    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.wallet2_view_loading_progress, this);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar1);
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
