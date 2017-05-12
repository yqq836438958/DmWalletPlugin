
package com.pacewear.tws.phoneside.wallet.ui2.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.ImageView;

import com.pacewear.tws.phoneside.wallet.R;
import com.pacewear.tws.phoneside.wallet.bean.OrderBean;
import com.pacewear.tws.phoneside.wallet.card.CardManager;
import com.pacewear.tws.phoneside.wallet.card.ICard;
import com.pacewear.tws.phoneside.wallet.order.IOrder;
import com.pacewear.tws.phoneside.wallet.order.OrderManager;
import com.pacewear.tws.phoneside.wallet.pay.PayManager;
import com.pacewear.tws.phoneside.wallet.ui.handler.WalletHandlerManager;
import com.pacewear.tws.phoneside.wallet.ui2.widget.CardBusinessLoadingView;

import TRom.E_PAY_SCENE;

import com.pacewear.tws.phoneside.wallet.ui.handler.WalletBaseHandler.ACTVITY_SCENE;
import com.pacewear.tws.phoneside.wallet.ui.handler.WalletBaseHandler.MODULE_CALLBACK;
import com.pacewear.tws.phoneside.wallet.ui.handler.WalletBaseHandler.OnWalletUICallback;

public class BusinessLoadingActivity extends TwsWalletActivity implements OnWalletUICallback {
    private final long DURATION_ISSUE = 4 * 60 * 1000;
    private final long DURATION_TOPUP = 2 * 60 * 1000;
    private OrderBean mOrderBean = null;
    private boolean mIsFirstIn = true;
    private Handler mUIHandler = null;
    public static final String KEY_ORDER_BEAN = "key_order_bean";
    public static final String KEY_EXE_RESULT = "key_exec_result";
    private boolean isTopupLoading = false;
    private Runnable mHandleResumeEvent = new Runnable() {
        @Override
        public void run() {
            onHandleResumeEvent();
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mUIHandler = new Handler();
        mOrderBean = (OrderBean) getIntent().getSerializableExtra(KEY_ORDER_BEAN);
        isTopupLoading = (mOrderBean.getPaySene() == E_PAY_SCENE._EPS_STAT);
        if (isTopupLoading) {
            setContentView(R.layout.wallet2_activity_loading_issue);
        } else {
            setContentView(R.layout.wallet2_activity_loading_topup);
        }
        initViews();
        if (!startBusisnessInternal(mOrderBean)) {
            finish();
        }
        WalletHandlerManager.getInstance().register(mOrderBean.getCardInstanceId(),
                ACTVITY_SCENE.SCENE_ISSE_TOPUP,
                this);
    }

    private void initViews() {
        hideActionBar();
        ImageView cardbg = (ImageView) findViewById(R.id.wallet2_loading_img);
        ICard card = CardManager.getInstance().getCard(mOrderBean.getCardInstanceId());
        cardbg.setImageResource(card.getCardLiteBg());
//        CardBusinessLoadingView loadingView = (CardBusinessLoadingView) findViewById(
//                R.id.cardloadingview);
//        if (isTopupLoading) {
//            loadingView.attach(DURATION_TOPUP);
//        } else {
//            loadingView.attach(DURATION_ISSUE);
//        }
    }

    @Override
    public void onResume() {
        super.onResume();
        WalletHandlerManager.getInstance().requestFocus(ACTVITY_SCENE.SCENE_ISSE_TOPUP);
        if (mIsFirstIn) {
            mIsFirstIn = false;
            return;
        }
        mUIHandler.postDelayed(mHandleResumeEvent, 2000);
    }

    @Override
    public void onPause() {
        super.onPause();
        mUIHandler.removeCallbacks(mHandleResumeEvent);
    }

    @Override
    public void onBackPressed() {
        // super.onBackPressed();
    }

    @Override
    public void onDestroy() {
        WalletHandlerManager.getInstance().unregister(ACTVITY_SCENE.SCENE_ISSE_TOPUP);
        super.onDestroy();
    }

    @Override
    public void onUIUpdate(MODULE_CALLBACK module, final int ret, boolean forUpdateUI) {
        Log.d("yqq", "onupdate:" + ret);
        mUIHandler.removeCallbacks(mHandleResumeEvent);
        mUIHandler.post(new Runnable() {
            @Override
            public void run() {
                Log.d("yqq", "gotoresultpage");
                gotoResultPage(ret);
            }
        });
    }

    private void onHandleResumeEvent() {
        if (PayManager.getInstanceInner().isPaying()) {
            PayManager.getInstanceInner().cancelPay();
            ICard card = CardManager.getInstance().getCard(mOrderBean.getCardInstanceId());
            IOrder order = OrderManager.getInstance()
                    .getLastOrder(card.getAID());
            if (order == null || order.isInValidOrder()) {
                gotoResultPage(-1);
                return;
            }
            OrderManager.getInstance().setOrderLocalPaidStatus(order.getOrderRspParam().sTradeNo,
                    true);
        }
    }

    private void gotoResultPage(int result) {
        Intent intent = new Intent(this, BusinessResultActivity.class);
        intent.putExtra(BusinessLoadingActivity.KEY_EXE_RESULT, result);
        intent.putExtra(KEY_ORDER_BEAN, mOrderBean);
        startActivity(intent);
        finish();
    }

    private boolean startBusisnessInternal(OrderBean bean) {
        long lReqId = -1L;
        if (bean == null) {
            return false;
        }
        if (isTopupLoading) {
            lReqId = OrderManager.getInstance().placeTopupOrder(bean.getCardInstanceId(),
                    bean.getPaySene(), bean.getPayType(), bean.getTopupFee(),
                    bean.isRetry());
        } else {
            lReqId = OrderManager.getInstance().placeIssueOrder(bean.getCardInstanceId(),
                    bean.getPaySene(), bean.getPayType(), bean.getIssueFee(), bean.getTopupFee(),
                    bean.isRetry());
        }
        return lReqId >= 0;
    }
}
