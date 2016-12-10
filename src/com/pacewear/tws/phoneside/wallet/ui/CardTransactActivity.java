
package com.pacewear.tws.phoneside.wallet.ui;

import android.app.TwsActivity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import com.pacewear.tws.phoneside.wallet.common.Utils;
import com.pacewear.tws.phoneside.wallet.transaction.CardTransactItem;
import com.pacewear.tws.phoneside.wallet.transaction.CardTransaction;
import com.pacewear.tws.phoneside.wallet.transaction.ITransactionCallback;
import com.pacewear.tws.phoneside.wallet.ui.widget.TransactionView;
import com.tencent.tws.pay.PayNFCConstants;

import java.util.ArrayList;

public class CardTransactActivity extends TwsActivity {
    private String mAid = null;
    private TransactionView mTransactionView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTransactionView = new TransactionView(this);
        setContentView(mTransactionView); // TODO
        init();
    }

    @Override
    protected void onDestroy() {
        Utils.getWorkerHandler().removeCallbacks(mQueryTransactRun);
        super.onDestroy();
    }

    private void init() {
        Intent intent = getIntent();
        mAid = intent.getStringExtra(PayNFCConstants.ExtraKeyName.EXTRA_STR_INSTANCE_ID);
        Utils.getWorkerHandler().removeCallbacks(mQueryTransactRun);
        if (!TextUtils.isEmpty(mAid)) {
            Utils.getWorkerHandler().post(mQueryTransactRun);
        }
    }

    private Runnable mQueryTransactRun = new Runnable() {

        @Override
        public void run() {
            CardTransaction transaction = new CardTransaction();
            transaction.query(mAid, new ITransactionCallback() {

                @Override
                public void onRsp(ArrayList<CardTransactItem> list) {
                    updateUI(list);
                }
            });
        }
    };

    private void updateUI(final ArrayList<CardTransactItem> list) {
        this.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                mTransactionView.fillData(list);
            }
        });
    }
}
