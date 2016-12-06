
package com.pacewear.tws.phoneside.wallet.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.pacewear.tws.phoneside.wallet.card.ICard.CARD_TYPE;

public class EmptyCard extends LinearLayout {

    protected static final String TAG = EmptyCard.class.getSimpleName();

    protected CARD_TYPE mType = CARD_TYPE.TRAFFIC_CARD;

    private FrameLayout mCardView = null;

    private TextView mCardTitle = null;

    private TextView mCardTip = null;

    public EmptyCard(Context context) {
        this(context, null);
    }

    public EmptyCard(Context context, AttributeSet attrs) {
        super(context, attrs);

        LayoutInflater.from(context).inflate(R.layout.wallet_empty_card, this);

        mCardView = (FrameLayout) findViewById(R.id.wallet_empty_card_body);
        mCardTitle = (TextView) findViewById(R.id.wallet_empty_add_title);
        mCardTip = (TextView) findViewById(R.id.wallet_empty_add_tip);
    }

    public void setType(CARD_TYPE type) {
        mType = type;
        switch (mType) {
            case TRAFFIC_CARD:
                mCardView.setBackgroundResource(R.drawable.wallet_traffic_card_empty);
                mCardTitle.setText(R.string.wallet_traffic_card_add);
                mCardTip.setText(R.string.select_add_traffic_card_tip);
                break;
            case BANK_CARD:
                mCardView.setBackgroundResource(R.drawable.wallet_bank_card_empty);
                mCardTitle.setText(R.string.wallet_bank_card_add);
                mCardTip.setText(R.string.select_add_bank_card_tip);
                break;
        }
    }
}
