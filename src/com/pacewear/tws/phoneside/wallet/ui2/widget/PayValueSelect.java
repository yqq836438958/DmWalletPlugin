
package com.pacewear.tws.phoneside.wallet.ui2.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;

import com.pacewear.tws.phoneside.wallet.R;

import java.util.ArrayList;

import TRom.PayRechargeAmount;

public class PayValueSelect extends FrameLayout implements OnClickListener {

    protected static final String TAG = PayValueSelect.class.getSimpleName();

    protected final Context mContext;

    private ArrayList<PayRechargeAmount> mPayRechargeAmounts = null;

    private Button mBTLeft = null;

    private Button mBTMiddle = null;

    private Button mBTRight = null;

    private Button mCurrentSelected = null;

    private int mBttonBgSelect = R.drawable.wallet_payselect_btn_select;

    private int mButtonBgUnSelect = R.drawable.wallet_payselect_btn_normal;
    private int mTextColorUnselected = getResources().getColor(
            R.color.wallet_charge_bt_text_unselected);

    private int mTextColorSelected = getResources()
            .getColor(R.color.wallet_charge_bt_text_selected);

    private OnSelectChangeListener mOnSelectChangeListener = null;

    public PayValueSelect(Context context) {
        this(context, null);
    }

    public PayValueSelect(Context context, AttributeSet attrs) {
        super(context, attrs);

        mContext = context;

        LayoutInflater.from(context).inflate(R.layout.wallet_pay_value_bar, this);

        mBTLeft = (Button) findViewById(R.id.pay_value_left_bt);
        mBTMiddle = (Button) findViewById(R.id.pay_value_middle_bt);
        mBTRight = (Button) findViewById(R.id.pay_value_right_bt);
        mBTLeft.setOnClickListener(this);
        mBTMiddle.setOnClickListener(this);
        mBTRight.setOnClickListener(this);

        notifySelectChanged(mBTLeft);
    }

    public void setPayRechargeAmount(ArrayList<PayRechargeAmount> payRechargeAmounts) {
        mPayRechargeAmounts = payRechargeAmounts;
        if (mPayRechargeAmounts != null && mPayRechargeAmounts.size() >= 3) {
            mBTLeft.setText(String.format(mContext.getString(R.string.wallet_pay_value_bar_yuan),
                    mPayRechargeAmounts.get(0).getITotalFee() / 100));
            mBTMiddle.setText(String.format(mContext.getString(R.string.wallet_pay_value_bar_yuan),
                    mPayRechargeAmounts.get(1).getITotalFee() / 100));
            mBTRight.setText(String.format(mContext.getString(R.string.wallet_pay_value_bar_yuan),
                    mPayRechargeAmounts.get(2).getITotalFee() / 100));
        }
    }

    @Override
    public void onClick(View view) {
        if (view == mBTLeft) {
        } else if (view == mBTMiddle) {
        } else if (view == mBTRight) {
        }

        notifySelectChanged(view);
    }

    private void notifySelectChanged(View current) {

        if (current != mCurrentSelected) {
            mBTLeft.setBackgroundResource(mButtonBgUnSelect);
            mBTLeft.setTextColor(mTextColorUnselected);
            mBTMiddle.setBackgroundResource(mButtonBgUnSelect);// (mColorUnselected);
            mBTMiddle.setTextColor(mTextColorUnselected);
            mBTRight.setBackgroundResource(mButtonBgUnSelect);
            mBTRight.setTextColor(mTextColorUnselected);
            mCurrentSelected = (Button) current;
            mCurrentSelected.setBackgroundResource(mBttonBgSelect);
            mCurrentSelected.setTextColor(mTextColorSelected);
            if (mOnSelectChangeListener != null) {
                if (mCurrentSelected == mBTLeft) {
                    mOnSelectChangeListener.onSelectChange(OnSelectChangeListener.LEFT_SELECTED);
                } else if (mCurrentSelected == mBTMiddle) {
                    mOnSelectChangeListener.onSelectChange(OnSelectChangeListener.MIDDLE_SELECTED);
                } else if (mCurrentSelected == mBTRight) {
                    mOnSelectChangeListener.onSelectChange(OnSelectChangeListener.RIGHT_SELECTED);
                }
            }
        }
    }

    public int getSelected() {
        int selected = OnSelectChangeListener.LEFT_SELECTED;
        if (mCurrentSelected == mBTLeft) {
            selected = OnSelectChangeListener.LEFT_SELECTED;
        } else if (mCurrentSelected == mBTMiddle) {
            selected = OnSelectChangeListener.MIDDLE_SELECTED;
        } else if (mCurrentSelected == mBTRight) {
            selected = OnSelectChangeListener.RIGHT_SELECTED;
        }

        return selected;
    }

    public void setSelect(int select) {
        View current = null;

        if (select == OnSelectChangeListener.LEFT_SELECTED) {
            current = mBTLeft;
        } else if (select == OnSelectChangeListener.MIDDLE_SELECTED) {
            current = mBTMiddle;
        } else {
            current = mBTRight;
        }

        notifySelectChanged(current);
    }

    public void setOnSelectChangeListener(OnSelectChangeListener listener) {
        mOnSelectChangeListener = listener;
    }

    public interface OnSelectChangeListener {
        public static final int LEFT_SELECTED = 1;

        public static final int MIDDLE_SELECTED = LEFT_SELECTED + 1;

        public static final int RIGHT_SELECTED = MIDDLE_SELECTED + 1;

        public void onSelectChange(int which);
    }
}
