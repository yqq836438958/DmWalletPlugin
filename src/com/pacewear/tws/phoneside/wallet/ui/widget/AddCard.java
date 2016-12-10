
package com.pacewear.tws.phoneside.wallet.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.example.plugindemo.R;

public class AddCard extends BaseCard {

    protected TextView mCardNumber = null;

    public AddCard(Context context) {
        this(context, null);
    }

    public AddCard(Context context, AttributeSet attrs) {
        super(context, attrs);

        inflate(context);
        initView();
    }

    @Override
    protected void inflate(Context context) {

        View view = LayoutInflater.from(context)
                .inflate(R.layout.wallet_base_add_card, this, false);

        if (view != null && view.getLayoutParams() != null) {
            LayoutParams lp = (LayoutParams) view.getLayoutParams();
            lp.gravity = Gravity.CENTER_HORIZONTAL;
        }

        addView(view);
    }

    @Override
    protected void initView() {
    }

    @Override
    protected void onCardAttached() {
    }

    @Override
    protected boolean isShowDefualtTag() {
        return false;
    }
}
