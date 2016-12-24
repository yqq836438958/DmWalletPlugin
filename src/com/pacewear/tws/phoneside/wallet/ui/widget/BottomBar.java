
package com.pacewear.tws.phoneside.wallet.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.pacewear.tws.phoneside.wallet.R;
import com.pacewear.tws.phoneside.wallet.common.UIHelper;
import com.tencent.tws.assistant.widget.TwsButton;

public class BottomBar extends FrameLayout implements OnClickListener {

    protected static final String TAG = BottomBar.class.getSimpleName();

    public static final int MODE_ERROR = -1;

    public static final int MODE_SINGLE_BUTTON = 1;

    public static final int MODE_COUPLE_BUTTON = MODE_SINGLE_BUTTON + 1;

    public static final int MODE_SINGLE_WITH_DESCRIPTION = MODE_COUPLE_BUTTON + 1;

    private int mMode = MODE_ERROR;

    TwsButton mSingleButton = null;

    View mCoupleButtonLayout = null;

    TwsButton mCoupleLeftButton = null;

    TwsButton mCoupleRightButton = null;

    View mSingleButtonWithDesLayout = null;

    TwsButton mSingleButtonWithDesButton = null;

    TextView mSingleMainDes = null;

    TextView mSingleSubDes = null;

    private OnBottomBarClickListener mOnBottomBarClickListener = null;

    public BottomBar(Context context) {
        this(context, null);
    }

    public BottomBar(Context context, AttributeSet attrs) {
        super(context, attrs);

        LayoutInflater.from(context).inflate(R.layout.wallet_bottom_bar, this);

        mSingleButton = (TwsButton) findViewById(R.id.br_single_bt);
        mSingleButton.setOnClickListener(this);

        mCoupleButtonLayout = findViewById(R.id.br_couple_bt_ly);

        mCoupleLeftButton = (TwsButton) findViewById(R.id.br_couple_left_bt);
        mCoupleLeftButton.setOnClickListener(this);

        mCoupleRightButton = (TwsButton) findViewById(R.id.br_couple_right_bt);
        mCoupleRightButton.setOnClickListener(this);

        mSingleButtonWithDesLayout = findViewById(R.id.br_single_bt_with_des_ly);

        mSingleButtonWithDesButton = (TwsButton) findViewById(R.id.br_single_bt_with_des_bt);
        mSingleButtonWithDesButton.setOnClickListener(this);
        UIHelper.setTwsButton(mSingleButton, R.string.wallet_traffic_card_add);
        UIHelper.setTwsButton(mCoupleLeftButton, R.string.wallet_traffic_card_add);
        UIHelper.setTwsButton(mCoupleRightButton, R.string.wallet_traffic_card_add);
        UIHelper.setTwsButton(mSingleButtonWithDesButton, R.string.activate_confirm);
        mSingleMainDes = (TextView) findViewById(R.id.br_single_bt_with_des_main);
        mSingleSubDes = (TextView) findViewById(R.id.br_single_bt_with_des_sub);

        setMode(MODE_SINGLE_BUTTON);
    }

    public void setMode(int mode) {

        if (mMode == mode) {
            return;
        }

        mMode = mode;

        mSingleButton.setVisibility(GONE);
        mCoupleButtonLayout.setVisibility(GONE);
        mSingleButtonWithDesLayout.setVisibility(GONE);
        switch (mMode) {
            case MODE_SINGLE_BUTTON:
                mSingleButton.setVisibility(VISIBLE);
                break;
            case MODE_COUPLE_BUTTON:
                mCoupleButtonLayout.setVisibility(VISIBLE);
                break;
            case MODE_SINGLE_WITH_DESCRIPTION:
                mSingleButtonWithDesLayout.setVisibility(VISIBLE);
                break;
        }
    }

    public void setSingleButtonText(int resId) {
        mSingleButton.setText(getResources().getString(resId));
    }

    public void setCoupleLeftButtonText(int resId) {
        mCoupleLeftButton.setText(getResources().getString(resId));
    }

    public void setmCoupleRightButtonText(int resId) {
        mCoupleRightButton.setText(getResources().getString(resId));
    }

    public void setSingleButtonWithDesButtonEnable(boolean enabled) {
        UIHelper.setTwsButtonEnable(mSingleButtonWithDesButton, enabled);
    }

    public void setSingleButtonWithDesButtonText(int resId) {
        mSingleButtonWithDesButton.setText(getResources().getString(resId));
    }

    public void setSingleButtonWithDesMainDes(String str) {
        mSingleMainDes.setText(str);
    }

    public void setOnBottomBarClickListener(OnBottomBarClickListener listener) {
        mOnBottomBarClickListener = listener;
    }

    public void setSingleSubDesText(String str) {
        mSingleSubDes.setText(str);
    }

    public static interface OnBottomBarClickListener {
        public boolean onSingleButtonClick();

        public boolean onCoupleLeftButtonClick();

        public boolean onCoupleRightButtonClick();

        public boolean onSingleButtonWithDecClick();
    }

    @Override
    public void onClick(View arg0) {

        if (mOnBottomBarClickListener == null) {
            return;
        }

        switch (arg0.getId()) {
            case R.id.br_single_bt:
                mOnBottomBarClickListener.onSingleButtonClick();
                break;
            case R.id.br_couple_left_bt:
                mOnBottomBarClickListener.onCoupleLeftButtonClick();
                break;
            case R.id.br_couple_right_bt:
                mOnBottomBarClickListener.onCoupleRightButtonClick();
                break;
            case R.id.br_single_bt_with_des_bt:
                mOnBottomBarClickListener.onSingleButtonWithDecClick();
                break;
        }
    }
}
