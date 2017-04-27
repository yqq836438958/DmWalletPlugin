
package com.pacewear.tws.phoneside.wallet.ui2.activity;

import android.app.TwsActivity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.pacewear.tws.phoneside.wallet.R;
import com.pacewear.tws.phoneside.wallet.card.CardManager;
import com.pacewear.tws.phoneside.wallet.card.ICard;
import com.pacewear.tws.phoneside.wallet.card.ICard.CARD_TYPE;
import com.pacewear.tws.phoneside.wallet.common.UIHelper;
import com.pacewear.tws.phoneside.wallet.order.IOrder;
import com.pacewear.tws.phoneside.wallet.order.OrderManager;
import com.pacewear.tws.phoneside.wallet.ui.ShowLoadingActivity;
import com.pacewear.tws.phoneside.wallet.ui.ShowOperationResultActivity;
import com.pacewear.tws.phoneside.wallet.ui2.toast.WalletErrToast;
import com.tencent.tws.assistant.widget.TwsButton;
import com.tencent.tws.pay.PayNFCConstants;

import TRom.E_PAY_SCENE;
import TRom.E_PAY_TYPE;
import qrom.component.log.QRomLog;

public class BusinessResultActivity extends TwsActivity {

    private ImageView mIcon;
    private TextView mTitle;
    private TextView mDesc;
    private TextView mRefundTip;
    private TwsButton mButton;
    public static final String TAG = ShowOperationResultActivity.class
            .getSimpleName();

    public static final String EXTRA_RESULT_TYPE = "EXTRA_RESULT_TYPE";

    public static final int RESULT_SUCCESS = 100;

    public static final int RESULT_FAILED = RESULT_SUCCESS + 1;

    private int mLoadingType = ShowLoadingActivity.LOADING_TYPE_NULL;

    private CARD_TYPE mType = CARD_TYPE.TRAFFIC_CARD;

    private ICard mCard = null;

    private long mTotalFee = 0;

    private int mPayType = E_PAY_TYPE._E_PT_WEIXIN_PAY;

    private int mPayScene = E_PAY_SCENE._EPS_OPEN_CARD;

    @Override
    public void finish() {
        super.finish();
        // overridePendingTransition(0, R.anim.wallet_push_down);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        // overridePendingTransition(R.anim.wallet_push_up, 0);

        super.onCreate(savedInstanceState);

        int resultType = RESULT_SUCCESS;
        String caption = null;
        String description = null;

        Intent intent = getIntent();
        if (intent != null) {
            int _type = intent
                    .getIntExtra(PayNFCConstants.ExtraKeyName.EXTRA_INT_CARDTYPE, 0);
            mType = CARD_TYPE.values()[_type];
            resultType = intent.getIntExtra(EXTRA_RESULT_TYPE, RESULT_SUCCESS);
            mInstanceId = intent
                    .getStringExtra(PayNFCConstants.ExtraKeyName.EXTRA_STR_INSTANCE_ID);
            mLoadingType = ShowLoadingActivity.getLoadingType(intent);
            mTotalFee = intent.getLongExtra(
                    PayNFCConstants.ExtraKeyName.EXTRA_LNG_TOTAL_FEE, 0L);
            mPayType = intent.getIntExtra(
                    PayNFCConstants.ExtraKeyName.EXTRA_INT_PAY_TYPE,
                    E_PAY_TYPE._E_PT_WEIXIN_PAY);
            mPayScene = intent.getIntExtra(PayNFCConstants.ExtraKeyName.EXTRA_INT_PAY_SCENE,
                    E_PAY_SCENE._EPS_OPEN_CARD);
        } else {
            finish();
            return;
        }

        setContentView(R.layout.wallet2_show_operation_result);

        mCard = CardManager.getInstance().getCard(mInstanceId);
        if (mCard == null) {
            finish();
            QRomLog.e(TAG, "onCreate|cardType=" + mType + ", instanceId="
                    + mInstanceId);
            return;
        }

        mIcon = (ImageView) findViewById(R.id.wallet_result_ic);
        mTitle = (TextView) findViewById(R.id.wallet_result_caption);
        mDesc = (TextView) findViewById(R.id.wallet_result_description);
        mButton = (TwsButton) findViewById(R.id.wallet_operation_result_close);
        mRefundTip = (TextView) findViewById(R.id.wallet_operation_refund);
        UIHelper.setTwsButton(mButton, R.string.wallet_operation_result_close, 14);
        mTitle.setText(caption);
        mDesc.setText(Html.fromHtml(description));
        mDesc.setMovementMethod(LinkMovementMethod.getInstance());
        IResultType result = (resultType == RESULT_SUCCESS) ? new OrderSuccessEvent()
                : (isOrderInValid() ? new OrderNoRetryEvent()
                        : new OrderCanRetryEvent());
        result.onCall();
    }

    private void onRetryClick() {
        QRomLog.d(TAG, "onRetryClick");
        ShowLoadingActivity.launchLoading(this, mCard.getCardType(),
                mCard.getAID(), mPayScene, mPayType, mTotalFee, mLoadingType,
                true);
        finish();
    }

    @Override
    public void onBackPressed() {
        // super.onBackPressed();
    }

    private boolean isOrderInValid() {
        IOrder order = OrderManager.getInstance().getLastOrder(mCard.getAID());
        return order == null || order.isInValidOrder();
    }

    interface IResultType {
        void onCall();
    }

    private class OrderSuccessEvent implements IResultType {
        @Override
        public void onCall() {
            mIcon.setImageResource(R.drawable.wallet_operate_success);
            mButton.setText(getString(R.string.wallet_payment_result_finish));
            mDesc.setText("余额xxxx元");
            mRefundTip.setVisibility(View.GONE);
            getTwsActionBar().hide();
            mButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
        }
    }

    private class OrderCanRetryEvent implements IResultType {
        @Override
        public void onCall() {
            mIcon.setImageResource(R.drawable.wallet_operate_failed);
            mButton.setText(getString(R.string.wallet_result_retry));
            mRefundTip.setVisibility(View.VISIBLE);
            mButton.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (WalletErrToast.checkAll(BusinessResultActivity.this)) {
                        return;
                    }
                    onRetryClick();
                }
            });
        }
    }

    private class OrderNoRetryEvent implements IResultType {
        @Override
        public void onCall() {
            mIcon.setImageResource(R.drawable.wallet_operate_failed);
            mButton.setText(getString(R.string.wallet_operation_result_close));
            mRefundTip.setVisibility(View.VISIBLE);
            mButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
        }

    }
}
