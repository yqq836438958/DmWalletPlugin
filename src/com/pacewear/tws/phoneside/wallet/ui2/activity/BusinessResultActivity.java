
package com.pacewear.tws.phoneside.wallet.ui2.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.pacewear.httpserver.IResponseObserver;
import com.pacewear.tws.phoneside.wallet.R;
import com.pacewear.tws.phoneside.wallet.WalletApp;
import com.pacewear.tws.phoneside.wallet.bean.OrderBean;
import com.pacewear.tws.phoneside.wallet.card.CardManager;
import com.pacewear.tws.phoneside.wallet.card.ICard;
import com.pacewear.tws.phoneside.wallet.common.ClickFilter;
import com.pacewear.tws.phoneside.wallet.common.Utils;
import com.pacewear.tws.phoneside.wallet.order.IOrder;
import com.pacewear.tws.phoneside.wallet.order.OrderManager;
import com.pacewear.tws.phoneside.wallet.tosservice.RequestRefund;
import com.pacewear.tws.phoneside.wallet.ui2.activity.BusinessResultHandler.BusinessContext;
import com.pacewear.tws.phoneside.wallet.ui2.activity.BusinessResultHandler.Result;
import com.pacewear.tws.phoneside.wallet.ui2.toast.WalletErrToast;
import com.qq.taf.jce.JceStruct;
import com.tencent.tws.assistant.widget.Toast;

import TRom.E_PAY_SCENE;
import TRom.OrderRspParam;
import TRom.RequestRefundRsp;
import qrom.component.log.QRomLog;

public class BusinessResultActivity extends TwsWalletActivity {
    public static final String TAG = BusinessResultActivity.class
            .getSimpleName();
    public static final String EXTRA_RESULT_TYPE = "EXTRA_RESULT_TYPE";
    public static final int RESULT_SUCCESS = 0;
    public static final int RESULT_FAILED = -1;
    private ImageView mTopupResultIcon;
    private ImageView mIssueResultIcon;
    private TextView mTitle;
    private TextView mDesc;
    private TextView mRefundTip;
    private Button mButton;
    private OrderBean mOrderBean = null;
    private View.OnClickListener mCloseEvent = new OnClickListener() {

        @Override
        public void onClick(View v) {
            finish();
        }
    };
    private View.OnClickListener mRetryEvent = new OnClickListener() {

        @Override
        public void onClick(View v) {
            if (WalletErrToast.checkAll(BusinessResultActivity.this)) {
                return;
            }
            onRetryClick();
        }
    };
    private View.OnClickListener mRefundEvent = new OnClickListener() {

        @Override
        public void onClick(View v) {
            if (ClickFilter.isMultiClick()) {
                return;
            }
            Utils.getWorkerHandler().removeCallbacks(mRefundRequest);
            Utils.getWorkerHandler().post(mRefundRequest);
        }
    };
    private Runnable mRefundRequest = new Runnable() {

        @Override
        public void run() {
            RequestRefund refund = new RequestRefund(getOrderTradeNo());
            final long reqId = refund.getUniqueSeq();
            refund.invoke(new IResponseObserver() {

                @Override
                public void onResponseSucceed(long uniqueSeq, int operType, JceStruct response) {
                    if (reqId == uniqueSeq) {
                        RequestRefundRsp rsp = (RequestRefundRsp) response;
                        showRefundResult(rsp != null && rsp.iRet == 0);
                    }
                }

                @Override
                public void onResponseFailed(long uniqueSeq, int operType, int errorCode,
                        String description) {
                    if (reqId == uniqueSeq) {
                        showRefundResult(false);
                    }
                }
            });
        }
    };

    private void showRefundResult(final boolean suc) {
        this.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                if (suc) {
                    getLastOrder().setInRefunding(true);
                    jumpRefundResultPage();
                } else {
                    Toast.makeText(WalletApp.getHostAppContext(), "fail", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void jumpRefundResultPage() {
        Intent intent = new Intent(BusinessResultActivity.this, RefundResultActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void finish() {
        super.finish();
        // overridePendingTransition(0, R.anim.wallet_push_down);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // overridePendingTransition(R.anim.wallet_push_up, 0);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wallet2_show_operation_result);
        initViews();
        init();
    }

    @Override
    protected void onDestroy() {
        Utils.getWorkerHandler().removeCallbacks(mRefundRequest);
        super.onDestroy();
    }

    private void initViews() {
        mTopupResultIcon = (ImageView) findViewById(R.id.wallet_result_ic);
        mIssueResultIcon = (ImageView) findViewById(R.id.wallet_issue_result_ic);
        mTitle = (TextView) findViewById(R.id.wallet_result_caption);
        mDesc = (TextView) findViewById(R.id.wallet_result_description);
        mButton = (Button) findViewById(R.id.wallet_operation_result_close);
        mRefundTip = (TextView) findViewById(R.id.wallet_operation_refund);
    }

    private void init() {
        Intent intent = getIntent();
        if (intent == null) {
            finish();
            return;
        }
        mOrderBean = (OrderBean) intent
                .getSerializableExtra(BusinessLoadingActivity.KEY_ORDER_BEAN);
        int resultType = intent.getIntExtra(BusinessLoadingActivity.KEY_EXE_RESULT, 0);
        boolean exeSuc = (resultType == RESULT_SUCCESS);
        boolean orderCanRetry = !exeSuc && !isOrderInValid();
        if (exeSuc) {
            setActionBar("", new NoTitleNoHideStagy());
        } else {
            setActionBar("", new LeftCancleRightHelpNoTitleStagy());
        }
        mTopupResultIcon.setImageResource(exeSuc ? R.drawable.wallet_operate_success
                : R.drawable.wallet_operate_failed);
        mTopupResultIcon.setVisibility(
                mOrderBean.getPaySene() == E_PAY_SCENE._EPS_STAT ? View.VISIBLE : View.GONE);
        mIssueResultIcon.setVisibility(
                mOrderBean.getPaySene() != E_PAY_SCENE._EPS_STAT ? View.VISIBLE : View.GONE);
        ImageView cardbg = (ImageView) findViewById(R.id.wallet_issue_result_ic);
        ICard card = CardManager.getInstance().getCard(mOrderBean.getCardInstanceId());
        cardbg.setImageResource(exeSuc ? card.getCardLiteBg() : card.getCardDisableLiteBg());
        Result filterResult = new BusinessResultHandler(this)
                .invoke(getBusinessContext(resultType));
        mTitle.setText(filterResult.getTitleRes());
        mDesc.setText(filterResult.getDescRes());
        mButton.setText((orderCanRetry)
                ? getString(R.string.wallet_result_retry)
                : getString(R.string.wallet_operation_result_close));
        mButton.setOnClickListener(orderCanRetry ? mRetryEvent : mCloseEvent);
        mRefundTip.setVisibility(orderCanRetry ? View.VISIBLE : View.GONE);
        mRefundTip.setOnClickListener(mRefundEvent);
        int toastRes = filterResult.getToastRes();
        if (toastRes != 0) {
            Toast.makeText(WalletApp.getHostAppContext(), getString(toastRes), Toast.LENGTH_LONG)
                    .show();
        }
    }

    private void onRetryClick() {
        QRomLog.d(TAG, "onRetryClick");
        Intent intent = new Intent(this, BusinessLoadingActivity.class);
        mOrderBean.setRetry(true);
        intent.putExtra(BusinessLoadingActivity.KEY_ORDER_BEAN, mOrderBean);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        // super.onBackPressed();
    }

    private boolean isOrderInValid() {
        IOrder order = getLastOrder();
        return order == null || order.isInValidOrder();
    }

    private IOrder getLastOrder() {
        String aid = mOrderBean.getCardInstanceId();
        return OrderManager.getInstance().getLastOrder(aid);
    }

    private String getOrderTradeNo() {
        IOrder order = getLastOrder();
        if (order == null) {
            return "";
        }
        OrderRspParam param = order.getOrderRspParam();
        if (param == null) {
            return "";
        }
        return param.getSTradeNo();
    }

    private BusinessContext getBusinessContext(int result) {
        String aid = mOrderBean.getCardInstanceId();
        BusinessContext context = new BusinessContext();
        context.card = CardManager.getInstance().getCard(aid);
        context.order = OrderManager.getInstance().getLastOrder(aid);
        context.invokeResult = result;
        context.isTopupInvoke = (mOrderBean.getPaySene() == E_PAY_SCENE._EPS_STAT);
        return context;
    }
}
