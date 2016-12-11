
package com.pacewear.tws.phoneside.wallet.ui;

import TRom.ApplyRefundRsp;
import TRom.E_PAY_TYPE;
import android.app.TwsActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.qq.taf.jce.JceStruct;
import com.tencent.tws.assistant.widget.Toast;
import com.tencent.tws.assistant.widget.TwsButton;
import com.tencent.tws.framework.global.GlobalObj;
import com.tencent.tws.pay.PayNFCConstants;
import com.pacewear.tws.phoneside.wallet.R;
import com.pacewear.tws.phoneside.wallet.card.CardManager;
import com.pacewear.tws.phoneside.wallet.card.ICard;
import com.pacewear.tws.phoneside.wallet.card.ICard.CARD_TYPE;
import com.pacewear.tws.phoneside.wallet.card.ICardInner.CONFIG;
import com.pacewear.tws.phoneside.wallet.common.Utils;
import com.pacewear.tws.phoneside.wallet.env.EnvManager;
import com.pacewear.tws.phoneside.wallet.order.IOrder;
import com.pacewear.tws.phoneside.wallet.order.OrderManager;
import com.pacewear.tws.phoneside.wallet.order.IOrderManager.ORDER_STEP;
import com.pacewear.tws.phoneside.wallet.tosservice.ApplyRefund;
import com.pacewear.tws.phoneside.wallet.tosservice.IResponseObserver;
import com.pacewear.tws.phoneside.wallet.wupserver.ServerHandler;

import qrom.component.log.QRomLog;

public class ShowOperationResultActivity extends TwsActivity {

    public static final String TAG = ShowOperationResultActivity.class
            .getSimpleName();

    public static final String EXTRA_RESULT_TYPE = "EXTRA_RESULT_TYPE";

    public static final int RESULT_SUCCESS = 100;

    public static final int RESULT_FAILED = RESULT_SUCCESS + 1;

    public static final String EXTRA_RESULT_CAPTION = "EXTRA_RESULT_CAPTION";

    public static final String EXTRA_RESULT_DESCRIPTION = "EXTRA_RESULT_DESCRIPTION";

    private int mLoadingType = ShowLoadingActivity.LOADING_TYPE_NULL;

    private CARD_TYPE mType = CARD_TYPE.TRAFFIC_CARD;

    private ICard mCard = null;

    private String mInstanceId;

    private long mTotalFee = 0;

    private int mPayType = E_PAY_TYPE._E_PT_WEIXIN_PAY;

    private Context mContext = null;

    private int mIconClickCount = 0;

    private static final int ICON_CLICK_COUNT = 15;

    @Override
    public void finish() {
        super.finish();
        // overridePendingTransition(0, R.anim.wallet_push_down);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        // overridePendingTransition(R.anim.wallet_push_up, 0);

        super.onCreate(savedInstanceState);

        mContext = this;

        int resultType = RESULT_SUCCESS;
        String caption = null;
        String description = null;

        Intent intent = getIntent();
        if (intent != null) {
            mType = (CARD_TYPE) intent.getSerializableExtra(
                    PayNFCConstants.ExtraKeyName.EXTRA_INT_CARDTYPE);
            resultType = intent.getIntExtra(EXTRA_RESULT_TYPE, RESULT_SUCCESS);
            caption = intent.getStringExtra(EXTRA_RESULT_CAPTION);
            description = intent.getStringExtra(EXTRA_RESULT_DESCRIPTION);
            mInstanceId = intent
                    .getStringExtra(PayNFCConstants.ExtraKeyName.EXTRA_STR_INSTANCE_ID);
            mLoadingType = ShowLoadingActivity.getLoadingType(intent);
            mTotalFee = intent.getLongExtra(
                    PayNFCConstants.ExtraKeyName.EXTRA_LNG_TOTAL_FEE, 0L);
            mPayType = intent.getIntExtra(
                    PayNFCConstants.ExtraKeyName.EXTRA_INT_PAY_TYPE,
                    E_PAY_TYPE._E_PT_WEIXIN_PAY);
        } else {
            finish();
            return;
        }

        setContentView(R.layout.wallet_show_operation_result);

        mCard = CardManager.getInstance().getCard(mInstanceId);
        if (mCard == null) {
            finish();
            QRomLog.e(TAG, "onCreate|cardType=" + mType + ", instanceId="
                    + mInstanceId);
            return;
        }

        final ImageView icon = (ImageView) findViewById(R.id.wallet_result_ic);
        TextView captionTv = (TextView) findViewById(R.id.wallet_result_caption);
        TextView descriptionTv = (TextView) findViewById(R.id.wallet_result_description);
        TextView retry = (TextView) findViewById(R.id.wallet_result_retry);
        TwsButton button = (TwsButton) findViewById(R.id.wallet_operation_result_close);

        if (!ServerHandler.getInstance().isTestEnv()) {
            captionTv.setEnabled(false);
        }
        captionTv.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                mIconClickCount++;
                if (mIconClickCount == ICON_CLICK_COUNT) {
                    Toast.makeText(mContext, "ApplyRefund already turnned on", Toast.LENGTH_LONG)
                            .show();
                }
            }
        });

        captionTv.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View arg0) {
                QRomLog.d(TAG, "onLongClick");

                if (mIconClickCount < ICON_CLICK_COUNT) {
                    mIconClickCount = 0;
                    return false;
                }

                final IOrder order = OrderManager.getInstance().getLastOrder(mCard.getAID());
                if (order == null) {
                    QRomLog.d(TAG, "getLastOrder null");
                    return false;
                }

                if (order.isIssueFail() || order.isCardTopFail()) {
                    QRomLog.d(TAG, "LastOrder is failed");

                    Utils.getWorkerHandler().post(new Runnable() {

                        public void showToast(final String toast) {
                            icon.post(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(mContext, toast, Toast.LENGTH_LONG)
                                            .show();
                                }
                            });
                        }

                        @Override
                        public void run() {
                            ApplyRefund applyRefund = new ApplyRefund();
                            applyRefund.setOrderReqParam(order.getOrderReqParam());
                            applyRefund.setOrderRspParam(order.getOrderRspParam());
                            applyRefund.setGetPayResultRspParam(order.getGetPayResultRspParam());

                            // 设置退款金额 应该放在后台
                            if (order.isIssueFail()) {
                                ORDER_STEP localStep = order.getOrderStep();
                                if (localStep == ORDER_STEP.EXECUTE_TOPUP) {
                                    applyRefund.setRefoundFee(order.getOrderReqParam().iTotalFee
                                            - order.getOrderReqParam().iOpenCardFee);
                                } else {
                                    applyRefund.setRefoundFee(order.getOrderReqParam().iTotalFee);
                                }
                            } else {
                                applyRefund.setRefoundFee(order.getOrderReqParam().iTotalFee);
                            }

                            boolean handled = applyRefund.invoke(new IResponseObserver() {

                                @Override
                                public void onResponseSucceed(long uniqueSeq, int operType,
                                        JceStruct response) {
                                    ApplyRefundRsp rsp = (ApplyRefundRsp) response;
                                    if (rsp.iRet == 0) {
                                        showToast("ApplyRefund succeed");
                                    } else {
                                        showToast("ApplyRefund failed");
                                    }
                                }

                                @Override
                                public void onResponseFailed(long uniqueSeq, int operType,
                                        int errorCode,
                                        String description) {
                                    showToast("ApplyRefund failed");
                                }
                            });

                            if (!handled) {
                                showToast("ApplyRefund failed");
                            }
                        }
                    });

                } else {
                    QRomLog.d(TAG, "LastOrder is ok");
                    return false;
                }

                return true;
            }
        });

        captionTv.setText(caption);
        descriptionTv.setText(Html.fromHtml(description));
        descriptionTv.setMovementMethod(LinkMovementMethod.getInstance());

        switch (resultType) {
            case RESULT_SUCCESS:
                icon.setImageResource(R.drawable.wallet_operate_success);
                button.setText(getString(R.string.wallet_payment_result_finish));
                break;
            case RESULT_FAILED:
                icon.setImageResource(R.drawable.wallet_operate_failed);
                button.setText(getString(R.string.wallet_operation_result_close));
                retry.setVisibility(View.VISIBLE);
                break;
            default:
                finish();
                return;
        }

        getTwsActionBar().hide();

        button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        retry.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (!EnvManager.getInstance().isWatchConnected()) {
                    Toast.makeText(GlobalObj.g_appContext, R.string.wallet_disconnect_tips,
                            Toast.LENGTH_LONG).show();
                    return;
                }
                if (!CardManager.getInstance().isReady()) {
                    Toast.makeText(mContext, R.string.wallet_sync_err_watch,
                            Toast.LENGTH_LONG).show();
                    return;
                }
                IOrder order = OrderManager.getInstance().getLastOrder(mCard.getAID());
                if (order == null || order.isInValidOrder()) {
                    Toast.makeText(GlobalObj.g_appContext, R.string.wallet_invalid_order,
                            Toast.LENGTH_LONG).show();
                    return;
                }
                onRetryClick();
            }
        });
    }

    private void onRetryClick() {
        QRomLog.d(TAG, "onRetryClick");

        switch (mLoadingType) {
            case ShowLoadingActivity.LOADING_TYPE_ACTIVATE_CARD:
            case ShowLoadingActivity.LOADING_TYPE_CHARGE_CARD:

                ShowLoadingActivity.launchLoading(this, mCard.getCardType(),
                        mCard.getAID(), mPayType, mTotalFee, mLoadingType,
                        true);
                finish();
                break;
        }
    }

    @Override
    public void onBackPressed() {
        // super.onBackPressed();
    }
}
