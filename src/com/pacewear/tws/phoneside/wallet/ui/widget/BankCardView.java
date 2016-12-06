
package com.pacewear.tws.phoneside.wallet.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.tencent.tws.gdevicemanager.R;
import com.tencent.tws.pay.PayNFCConstants;

public class BankCardView extends BaseCard {

    private View mBankNumberLayout = null;

    private TextView mCardNumber = null;

    // 产品有需求只有一张卡片不显示默认标签
    public static boolean sShowDefaultTag = true;

    public BankCardView(Context context) {
        this(context, null);
    }

    public BankCardView(Context context, AttributeSet attrs) {
        super(context, attrs);

        mType = PayNFCConstants.Card.TYPE_BANK;

        inflate(context);
        initView();
    }

    @Override
    protected void inflate(Context context) {
        View view = LayoutInflater.from(context).inflate(R.layout.wallet_base_bank_card, this,
                false);

        if (view != null && view.getLayoutParams() != null) {
            LayoutParams lp = (LayoutParams) view.getLayoutParams();
            lp.gravity = Gravity.CENTER_HORIZONTAL;
        }

        addView(view);
    }

    @Override
    protected void initView() {
        super.initView();

        mBankNumberLayout = findViewById(R.id.bank_card_number_layout);
        mCardNumber = (TextView) findViewById(R.id.card_number);
    }

    @Override
    protected void onCardAttached() {
        super.onCardAttached();
        mCardNumber.setText(mCardAttached.getCardNumber());
    }

    @Override
    public void showShadeText(int resId) {
        super.showShadeText(resId);

        mBankNumberLayout.setVisibility(View.GONE);
    }

    @Override
    public void hideFaceShade() {
        super.hideFaceShade();

        mBankNumberLayout.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideWhenShowDetail(boolean hide) {
        super.hideWhenShowDetail(hide);
    }

    @Override
    protected boolean isShowDefualtTag() {
        return sShowDefaultTag;
    }

    public static void setShowDefaultTag(boolean show) {
        sShowDefaultTag = show;
    }
}
