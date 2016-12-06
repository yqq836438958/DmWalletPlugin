
package com.tencent.tws.phoneside.walletv2.ui;

import android.app.TwsActivity;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.TextView;

import com.pacewear.tws.phoneside.wallet.card.CardManager;
import com.pacewear.tws.phoneside.wallet.card.ICard;
import com.pacewear.tws.phoneside.wallet.ui.widget.BaseCard;
import com.tencent.tws.assistant.app.ActionBar;
import com.tencent.tws.pay.PayNFCConstants;

public class ErrorCardActivity extends TwsActivity {
    private BaseCard mErrorCardView = null;
    private TextView mErrorDescTextView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wallet_error_card_page);
        initViews();
        init();
    }

    private void init() {
        Intent intent = getIntent();
        String aid = intent.getStringExtra(PayNFCConstants.ExtraKeyName.EXTRA_STR_INSTANCE_ID);
        ICard card = null;
        if (!TextUtils.isEmpty(aid)) {
            card = CardManager.getInstance().getCard(aid);
        }
        if (card != null) {
            ActionBar actionBar = getTwsActionBar();
            actionBar.setBackgroundDrawable(new ColorDrawable(getResources()
                    .getColor(R.color.wallet_action_bar_background)));
            actionBar.setTitle(card.getCardName());
            mErrorDescTextView.setText(card.getCardInfoErrDesc());
            mErrorCardView.attachCard(card);
            mErrorCardView.showShadeText("");
        }
    }

    private void initViews() {
        mErrorCardView = (BaseCard) findViewById(R.id.wallet_card_detail_card);
        mErrorDescTextView = (TextView) findViewById(R.id.wallet_error_card_desc);
    }
}
