
package com.pacewear.tws.phoneside.wallet.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import qrom.component.log.QRomLog;

import com.example.plugindemo.R;
import com.pacewear.tws.phoneside.wallet.card.ITrafficCard;
import com.pacewear.tws.phoneside.wallet.common.Utils;
import com.tencent.tws.pay.PayNFCConstants;

public class TrafficCardView extends BaseCard {

    private static final String TAG = TrafficCardView.class.getSimpleName();

    protected View mTrafficBalanceLayout = null;

    protected ImageView mBalanceIcon = null;

    protected TextView mTrafficCardBalance = null;

    // 产品有需求只有一张卡片不显示默认标签
    public static boolean sShowDefaultTag = true;

    public TrafficCardView(Context context) {
        this(context, null);
    }

    public TrafficCardView(Context context, AttributeSet attrs) {
        super(context, attrs);

        mType = PayNFCConstants.Card.TYPE_TRAFFIC;

        inflate(context);
        initView();

    }

    @Override
    protected void inflate(Context context) {
        View view = LayoutInflater.from(context).inflate(R.layout.wallet_base_traffic_card, this,
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

        mTrafficBalanceLayout = findViewById(R.id.traffic_card_balance_layout);
        mBalanceIcon = (ImageView) findViewById(R.id.iv_wallet_balance_ic);
        mTrafficCardBalance = (TextView) findViewById(R.id.traffic_card_balance);
    }

    @Override
    protected void onCardAttached() {
        super.onCardAttached();
        mBalanceIcon.setImageResource(((ITrafficCard) mCardAttached).getBalanceUnitIcon());
        mTrafficCardBalance.setTextColor(
                getResources().getColor(((ITrafficCard) mCardAttached).getBalanceTextColor()));
        mTrafficCardBalance
                .setText(Utils.getDisplayBalance(((ITrafficCard) mCardAttached).getBalance()));
    }

    @Override
    public void showShadeText(int resId) {
        super.showShadeText(resId);
        mTrafficBalanceLayout.setVisibility(View.GONE);
    }

    @Override
    public void hideFaceShade() {
        super.hideFaceShade();

        if (!mHideWhenShowDetail) {
            mTrafficBalanceLayout.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void hideWhenShowDetail(boolean hide) {
        super.hideWhenShowDetail(hide);

        if (mHideWhenShowDetail) {
            mTrafficBalanceLayout.setVisibility(View.GONE);
        } else {
            mTrafficBalanceLayout.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected boolean isShowDefualtTag() {
        return sShowDefaultTag;
    }

    public static void setShowDefaultTag(boolean show) {
        sShowDefaultTag = show;
    }
}
