
package com.pacewear.tws.phoneside.wallet.ui;

import TRom.E_PAY_SCENE;
import TRom.E_PAY_TYPE;
import TRom.OrderReqParam;
import android.R.integer;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.app.TwsActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.WindowManager;

import com.tencent.tws.assistant.widget.Toast;
import com.tencent.tws.framework.global.GlobalObj;
import com.tencent.tws.pay.PayNFCConstants;
import com.pacewear.tws.phoneside.wallet.R;
import com.pacewear.tws.phoneside.wallet.card.CardManager;
import com.pacewear.tws.phoneside.wallet.card.ICard;
import com.pacewear.tws.phoneside.wallet.card.ICard.CARD_TYPE;
import com.pacewear.tws.phoneside.wallet.card.ICard.INSTALL_STATUS;
import com.pacewear.tws.phoneside.wallet.card.ICardInner.CONFIG;
import com.pacewear.tws.phoneside.wallet.common.Utils;
import com.pacewear.tws.phoneside.wallet.card.ITrafficCard;
import com.pacewear.tws.phoneside.wallet.order.IOrder;
import com.pacewear.tws.phoneside.wallet.order.IOrderManager;
import com.pacewear.tws.phoneside.wallet.order.IOrderManager.ORDER_STEP;
import com.pacewear.tws.phoneside.wallet.order.OrderManager;
import com.pacewear.tws.phoneside.wallet.pay.PayManager;
import com.pacewear.tws.phoneside.wallet.step.IStep.COMMON_STEP;
import com.pacewear.tws.phoneside.wallet.step.IStep.STATUS;
import com.pacewear.tws.phoneside.wallet.ui.handler.WalletHandlerManager;
import com.pacewear.tws.phoneside.wallet.ui.handler.WalletBaseHandler.ACTVITY_SCENE;
import com.pacewear.tws.phoneside.wallet.ui.handler.WalletBaseHandler.MODULE_CALLBACK;
import com.pacewear.tws.phoneside.wallet.ui.handler.WalletBaseHandler.OnWalletUICallback;
import com.pacewear.tws.phoneside.wallet.ui.widget.LoadingBubble;

import qrom.component.log.QRomLog;

public class ShowLoadingActivity extends TwsActivity
        implements OnWalletUICallback {

    public static final String TAG = PayNFCConstants.TAG + "."
            + ShowLoadingActivity.class.getSimpleName();

    private static String LOADING_TYPE = "loading_type";

    public static final int LOADING_TYPE_NULL = -1;

    public static final int LOADING_TYPE_ACTIVATE_CARD = 1;

    public static final int LOADING_TYPE_CHARGE_CARD = LOADING_TYPE_ACTIVATE_CARD
            + 1;

    private int mLoadingType = LOADING_TYPE_ACTIVATE_CARD;

    private CARD_TYPE mType = CARD_TYPE.TRAFFIC_CARD;
    private boolean mIsRerty = false;
    private String mInstanceId;
    private long mActiveMoney = 0;
    private long mTotalMoney = 0;
    private int mPayType = E_PAY_TYPE._E_PT_WEIXIN_PAY;

    private Context mContext = null;

    private ICard mCard = null;

    private ValueAnimator mValueAnimator = null;

    private static final long DEFALUT_TIME = 2 * 60 * 1000;

    private static final int PERCENT_MAX_VALUE = 99;

    private LoadingBubble mLoadingBubble = null;
    private Handler mMainUIHandler = null;
    private boolean mIsFirstIn = true;

    public static void setLoadingType(Intent intent, int loadingType) {
        if (intent != null) {
            intent.putExtra(LOADING_TYPE, loadingType);
        }
    }

    public static void launchLoading(Context context, CARD_TYPE cardType,
            String instanceId, int payType, long chargeFee, int loadingType,
            boolean retry) {
        launchLoading(context, cardType, instanceId, payType, 0, chargeFee,
                loadingType, retry);
    }

    public static void launchLoading(Context context, CARD_TYPE card_TYPE,
            String instanceId, int payType, long activeFee, long totalFee,
            int loadingType, boolean retry) {
        Intent intent = new Intent(context, ShowLoadingActivity.class);

        intent.putExtra(PayNFCConstants.ExtraKeyName.EXTRA_INT_CARDTYPE,
                card_TYPE);
        intent.putExtra(PayNFCConstants.ExtraKeyName.EXTRA_STR_INSTANCE_ID,
                instanceId);
        intent.putExtra(PayNFCConstants.ExtraKeyName.EXTRA_INT_PAY_TYPE,
                payType);
        if (activeFee > 0) {
            intent.putExtra(PayNFCConstants.ExtraKeyName.EXTRA_LNG_OPENCARD_FEE,
                    activeFee);
        }
        intent.putExtra(PayNFCConstants.ExtraKeyName.EXTRA_LNG_TOTAL_FEE,
                totalFee);
        intent.putExtra(PayNFCConstants.ExtraKeyName.EXTRA_BOOL_IS_RETRY,
                retry);

        ShowLoadingActivity.setLoadingType(intent, loadingType);

        context.startActivity(intent);
    }

    public static int getLoadingType(Intent intent) {
        if (intent == null) {
            return LOADING_TYPE_NULL;
        }
        return intent.getIntExtra(LOADING_TYPE, LOADING_TYPE_NULL);
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

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mContext = this;
        mMainUIHandler = new Handler();
        Intent intent = getIntent();
        if (intent != null) {
            mType = (CARD_TYPE) intent.getSerializableExtra(
                    PayNFCConstants.ExtraKeyName.EXTRA_INT_CARDTYPE);
            mIsRerty = intent.getBooleanExtra(
                    PayNFCConstants.ExtraKeyName.EXTRA_BOOL_IS_RETRY, false);
            mInstanceId = intent.getStringExtra(
                    PayNFCConstants.ExtraKeyName.EXTRA_STR_INSTANCE_ID);
            mTotalMoney = intent.getLongExtra(
                    PayNFCConstants.ExtraKeyName.EXTRA_LNG_TOTAL_FEE, 0);
            mActiveMoney = intent.getLongExtra(
                    PayNFCConstants.ExtraKeyName.EXTRA_LNG_OPENCARD_FEE, 0);
            mPayType = intent.getIntExtra(
                    PayNFCConstants.ExtraKeyName.EXTRA_INT_PAY_TYPE,
                    E_PAY_TYPE._E_PT_WEIXIN_PAY);

            QRomLog.d(TAG,
                    "onCreate|mType=" + mType + ",mInstanceId=" + mInstanceId
                            + ",mTotalMoney=" + mTotalMoney + ",mPayType=" + mPayType);

            mCard = CardManager.getInstance().getCard(mInstanceId);

            if (mCard == null) {
                finish();
                QRomLog.e(TAG, "onCreate|cardType=" + mType + ", instanceId="
                        + mInstanceId);
                return;
            }
            mLoadingType = getLoadingType(intent);
        }

        setContentView(R.layout.wallet_show_loading);

        mLoadingBubble = (LoadingBubble) findViewById(
                R.id.wallet_loading_bubble);

        mValueAnimator = ValueAnimator.ofInt(0, PERCENT_MAX_VALUE);
        mValueAnimator.setDuration(DEFALUT_TIME);
        mValueAnimator.addUpdateListener(new AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator arg0) {
                // TODO Auto-generated method stub
                int progress = (Integer) arg0.getAnimatedValue();
                mLoadingBubble.setPercentTage(progress);

            }
        });
        mValueAnimator.start();
        if (mLoadingType == LOADING_TYPE_CHARGE_CARD) {
            mLoadingBubble.setCustomLoadingNotice(getString(R.string.wallet_charge_loading));
        } else if (mLoadingType == LOADING_TYPE_ACTIVATE_CARD) {
            mLoadingBubble.setCustomLoadingNotice(getString(R.string.wallet_activate_loading));
        }
        boolean suc = resumeOpenOrTopupCard(mIsRerty);
        if (!suc) {
            finish();
            return;
        }

        getTwsActionBar().hide();
        WalletHandlerManager.getInstance().register(mCard.getAID(), ACTVITY_SCENE.SCENE_ISSE_TOPUP,
                this);
    }

    @Override
    public void onDestroy() {
        WalletHandlerManager.getInstance().unregister(ACTVITY_SCENE.SCENE_ISSE_TOPUP);
        if (mValueAnimator != null) {
            mValueAnimator.cancel();
        }
        super.onDestroy();
    }

    private void showResult(int rc) {
        // 重新加载
        mCard = CardManager.getInstance().getCard(mInstanceId);
        IOrder order = OrderManager.getInstance().getLastOrder(mCard.getAID());
        if (order == null) {
            finishAndToast(R.string.wallet_no_order);
            return;
        }
        if (order.isInValidOrder()) {
            finishAndToast(R.string.wallet_invalid_order);
        }
        if (mCard == null) {
            finishAndToast(R.string.wallet_sync_err_watch);
            return;
        }
        int orderfinish = -1;
        String caption = null;
        ORDER_STEP localStep = order.getOrderStep();
        if (mLoadingType == ShowLoadingActivity.LOADING_TYPE_ACTIVATE_CARD) {
            Intent intent = new Intent(mContext,
                    ShowOperationResultActivity.class);
            intent.putExtra(PayNFCConstants.ExtraKeyName.EXTRA_INT_CARDTYPE,
                    mCard.getCardType());
            intent.putExtra(PayNFCConstants.ExtraKeyName.EXTRA_STR_INSTANCE_ID,
                    mCard.getAID());

            if (rc == 0) {
                orderfinish = 1;
                caption = getString(R.string.activate_card_succeed);
            } else {
                if (localStep == ORDER_STEP.EXECUTE_TOPUP
                        && mCard.getInstallStatus() == INSTALL_STATUS.PERSONAL) {
                    orderfinish = -1;
                    caption = getString(
                            R.string.wallet_activate_card_topup_failed);
                } else if (localStep == ORDER_STEP.ORDER_FINISH) {
                    orderfinish = 0;
                    caption = getString(R.string.wallet_activate_card_query_failed);
                } else {
                    orderfinish = -1;
                    caption = getString(R.string.wallet_activate_card_failed);
                }
            }

            intent.putExtra(ShowOperationResultActivity.EXTRA_RESULT_CAPTION,
                    mCard.getCardName() + caption);
            intent.putExtra(
                    ShowOperationResultActivity.EXTRA_RESULT_DESCRIPTION,
                    rc == 0
                            ? getChargeBalanceTips()
                            : getErrorDesc(caption, order));
            intent.putExtra(PayNFCConstants.ExtraKeyName.EXTRA_INT_PAY_TYPE,
                    mPayType);
            intent.putExtra(PayNFCConstants.ExtraKeyName.EXTRA_LNG_TOTAL_FEE,
                    order.getOrderReqParam().getITotalFee());
            intent.putExtra(ShowOperationResultActivity.EXTRA_RESULT_TYPE,
                    (orderfinish >= 0)
                            ? ShowOperationResultActivity.RESULT_SUCCESS
                            : ShowOperationResultActivity.RESULT_FAILED);
            setLoadingType(intent, mLoadingType);

            mContext.startActivity(intent);
        } else if (mLoadingType == ShowLoadingActivity.LOADING_TYPE_CHARGE_CARD) {
            Intent intent = new Intent(mContext,
                    ShowOperationResultActivity.class);
            intent.putExtra(PayNFCConstants.ExtraKeyName.EXTRA_INT_CARDTYPE,
                    mCard.getCardType());
            intent.putExtra(PayNFCConstants.ExtraKeyName.EXTRA_STR_INSTANCE_ID,
                    mCard.getAID());
            if (rc == 0) {
                orderfinish = 1;
                caption = getString(R.string.wallet_operation_charge_succeed);
            } else if (localStep == ORDER_STEP.ORDER_FINISH) {
                orderfinish = 0;
                caption = getString(R.string.wallet_topup_card_query_failed);
            } else {
                orderfinish = -1;
                caption = getString(R.string.wallet_operation_charge_failed);
            }
            intent.putExtra(ShowOperationResultActivity.EXTRA_RESULT_TYPE,
                    (orderfinish >= 0)
                            ? ShowOperationResultActivity.RESULT_SUCCESS
                            : ShowOperationResultActivity.RESULT_FAILED);
            intent.putExtra(ShowOperationResultActivity.EXTRA_RESULT_CAPTION,
                    mCard.getCardName() + caption);
            intent.putExtra(
                    ShowOperationResultActivity.EXTRA_RESULT_DESCRIPTION,
                    rc == 0
                            ? getChargeBalanceTips()
                            : getErrorDesc(caption, order));
            intent.putExtra(PayNFCConstants.ExtraKeyName.EXTRA_INT_PAY_TYPE,
                    mPayType);
            intent.putExtra(PayNFCConstants.ExtraKeyName.EXTRA_LNG_TOTAL_FEE,
                    order.getOrderReqParam().getITotalFee());
            setLoadingType(intent, mLoadingType);

            mContext.startActivity(intent);
        }
        finish();
    }

    @Override
    public void onBackPressed() {
        // super.onBackPressed();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMainUIHandler.removeCallbacks(mHandleResumeEvent);
    }

    @Override
    public void onResume() {
        super.onResume();
        WalletHandlerManager.getInstance().requestFocus(ACTVITY_SCENE.SCENE_ISSE_TOPUP);
        if (mIsFirstIn) {
            mIsFirstIn = false;
            return;
        }
        mMainUIHandler.postDelayed(mHandleResumeEvent, 2000);
    }

    private Runnable mHandleResumeEvent = new Runnable() {
        @Override
        public void run() {
            onHandleResumeEvent();
        }
    };

    private void onHandleResumeEvent() {
        if (PayManager.getInstanceInner().isPaying()) {
            PayManager.getInstanceInner().cancelPay();
            ICard card = CardManager.getInstance().getCard(mInstanceId);
            if (card == null) {
                QRomLog.e(TAG, "cardTopupReq|get cardHolder fail. ");
                finishAndToast(R.string.wallet_sync_err_watch);
                return;
            }
            IOrder order = OrderManager.getInstance()
                    .getLastOrder(card.getAID());
            if (order == null) {
                finishAndToast(R.string.wallet_no_order);
                return;
            }
            if (order.isInValidOrder()) {
                finishAndToast(R.string.wallet_invalid_order);
            }
            OrderManager.getInstance().setOrderLocalPaidStatus(order.getOrderRspParam().sTradeNo,
                    true);
        }
    }

    private boolean resumeOpenOrTopupCard(boolean retry) {
        long ret = 0;
        switch (mLoadingType) {
            case LOADING_TYPE_ACTIVATE_CARD:
                ret = OrderManager.getInstance().placeIssueOrder(mInstanceId,
                        mPayType, mActiveMoney, mTotalMoney - mActiveMoney, retry);
                break;
            case LOADING_TYPE_CHARGE_CARD:
                ret = OrderManager.getInstance().placeTopupOrder(mInstanceId,
                        mPayType, mTotalMoney - mActiveMoney, retry);
                break;
        }
        return (ret >= 0);
    }

    private void finishAndToast(int strRes) {
        Toast.makeText(GlobalObj.g_appContext, getString(strRes), Toast.LENGTH_LONG)
                .show();
        finish();
    }

    private String getErrorDesc(String caption, IOrder order) {
        String sErrCode = order.getBusinessErr();
        boolean showErrCode = !TextUtils.isEmpty(sErrCode);
        String sErrDesc = caption
                + String.format(getString(R.string.wallet_errcode_desc), sErrCode);
        String commonTips = order.isInValidOrder() ? getString(R.string.wallet_invalid_order)
                : getString(R.string.wallet_operation_failed_tip);
        return showErrCode ? sErrDesc : commonTips;
    }

    private String getChargeBalanceTips() {
        long iCurBalance = getCurCardBalance();
        String newbalance = Utils.getDisplayBalance(iCurBalance);
        return String.format(getString(R.string.wallet_traffic_card_balance_charge), newbalance);
    }

    public long getCurCardBalance() {
        String strBalance = ((ITrafficCard) mCard).getBalance();
        if (TextUtils.isEmpty(strBalance)) {
            return 0;
        }
        return Long.parseLong(strBalance);
    }

    @Override
    public void onUIUpdate(MODULE_CALLBACK module, final int ret, boolean forUpdateUI) {
        mMainUIHandler.removeCallbacks(mHandleResumeEvent);
        mMainUIHandler.post(new Runnable() {
            @Override
            public void run() {
                showResult(ret);
            }
        });
    }

}
