
package com.pacewear.tws.phoneside.wallet.ui2.widget;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import com.pacewear.tws.phoneside.wallet.R;
import com.pacewear.tws.phoneside.wallet.card.ITrafficCard;
import com.pacewear.tws.phoneside.wallet.common.FontsOverride;
import com.pacewear.tws.phoneside.wallet.common.Utils;

public class TrafficCardView extends BaseCardView {
    private TextView mTrafficCardBalance = null;

    public TrafficCardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TrafficCardView(Context context) {
        this(context, null);
    }

    @Override
    protected int getFlatLayout() {
        return R.layout.wallet2_base_traffic_card;
    }

    @Override
    protected void onPostInit() {
        mTrafficCardBalance = (TextView) findViewById(R.id.traffic_card_balance);
        mTrafficCardBalance.setTypeface(FontsOverride.getDigitFont(mContext));
    }

    @Override
    protected void onUpdate(int type) {
        String amount = Utils.getDisplayBalance(((ITrafficCard) mCardAttached).getBalance());
        if (mTrafficCardBalance != null || type == BaseViewHandler.ISSUEFAIL) {
            mTrafficCardBalance.setVisibility(TextUtils.isEmpty(amount) ? View.GONE : View.VISIBLE);
            mTrafficCardBalance.setText("￥" + amount);
        }
    }
}
