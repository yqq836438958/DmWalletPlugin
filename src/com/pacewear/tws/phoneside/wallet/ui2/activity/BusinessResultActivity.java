
package com.pacewear.tws.phoneside.wallet.ui2.activity;

import android.app.TwsActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.pacewear.tws.phoneside.wallet.R;
import com.pacewear.tws.phoneside.wallet.WalletApp;
import com.pacewear.tws.phoneside.wallet.bean.OrderBean;
import com.pacewear.tws.phoneside.wallet.card.CardManager;
import com.pacewear.tws.phoneside.wallet.common.UIHelper;
import com.pacewear.tws.phoneside.wallet.order.IOrder;
import com.pacewear.tws.phoneside.wallet.order.OrderManager;
import com.pacewear.tws.phoneside.wallet.ui2.activity.BusinessResultHandler.BusinessContext;
import com.pacewear.tws.phoneside.wallet.ui2.activity.BusinessResultHandler.Result;
import com.pacewear.tws.phoneside.wallet.ui2.toast.WalletErrToast;
import com.tencent.tws.assistant.app.ActionBar;
import com.tencent.tws.assistant.widget.Toast;

import TRom.E_PAY_SCENE;
import TRom.E_PAY_TYPE;
import qrom.component.log.QRomLog;

public class BusinessResultActivity extends TwsActivity {
    public static final String TAG = BusinessResultActivity.class
            .getSimpleName();
    public static final String EXTRA_RESULT_TYPE = "EXTRA_RESULT_TYPE";
    public static final int RESULT_SUCCESS = 0;
    public static final int RESULT_FAILED = -1;
    private ImageView mIcon;
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

    private void initViews() {
        mIcon = (ImageView) findViewById(R.id.wallet_result_ic);
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
        ActionBar actionBar = getTwsActionBar();
        actionBar.setTitle("");
        Button actionLeftBt = (Button) actionBar.getCloseView(false);
        actionLeftBt.setText(getResources().getString(R.string.wallet_select_default_cancel));
        actionLeftBt.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                finish();
            }
        });
        if (exeSuc) {
            actionBar.hide();
        }
        mIcon.setImageResource(exeSuc ? R.drawable.wallet_operate_success
                : R.drawable.wallet_operate_failed);
        Result tilteResult = new BusinessResultHandler().invoke(getBusinessContext(resultType));
        mTitle.setText(tilteResult.getTitleRes());
        mDesc.setText(R.string.wallet_operation_failed_tip);
        mButton.setText((orderCanRetry)
                ? getString(R.string.wallet_result_retry)
                : getString(R.string.wallet_operation_result_close));
        mButton.setOnClickListener(orderCanRetry ? mRetryEvent : mCloseEvent);
        mRefundTip.setVisibility(orderCanRetry ? View.VISIBLE : View.GONE);
        int toastRes = tilteResult.getToastRes();
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
        String aid = mOrderBean.getCardInstanceId();
        IOrder order = OrderManager.getInstance().getLastOrder(aid);
        return order == null || order.isInValidOrder();
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
