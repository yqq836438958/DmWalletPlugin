
package com.pacewear.tws.phoneside.wallet.ui;

import TRom.GetCustomServiceRsp;

import android.app.TwsActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.plugindemo.R;
import com.pacewear.tws.phoneside.wallet.card.CardManager;
import com.pacewear.tws.phoneside.wallet.card.ICard;
import com.pacewear.tws.phoneside.wallet.card.ICard.ACTIVATION_STATUS;
import com.pacewear.tws.phoneside.wallet.card.ICard.CARD_TYPE;
import com.pacewear.tws.phoneside.wallet.card.ICardInner.CONFIG;
import com.pacewear.tws.phoneside.wallet.card.ITrafficCard;
import com.pacewear.tws.phoneside.wallet.common.Utils;
import com.pacewear.tws.phoneside.wallet.env.EnvManager;
import com.pacewear.tws.phoneside.wallet.order.IOrder;
import com.pacewear.tws.phoneside.wallet.order.OrderManager;
import com.pacewear.tws.phoneside.wallet.tosservice.IResponseObserver;
import com.pacewear.tws.phoneside.wallet.tosservice.PullUserInfo;
import com.pacewear.tws.phoneside.wallet.ui.handler.WalletBaseHandler.ACTVITY_SCENE;
import com.pacewear.tws.phoneside.wallet.ui.handler.WalletBaseHandler.MODULE_CALLBACK;
import com.pacewear.tws.phoneside.wallet.ui.handler.WalletBaseHandler.OnWalletUICallback;
import com.pacewear.tws.phoneside.wallet.ui.handler.WalletHandlerManager;
import com.pacewear.tws.phoneside.wallet.ui.widget.BaseCard;
import com.qq.taf.jce.JceStruct;
import com.tencent.tws.assistant.app.ActionBar;
import com.tencent.tws.assistant.app.AlertDialog;
import com.tencent.tws.assistant.app.TwsDialog;
import com.tencent.tws.assistant.widget.Toast;
import com.tencent.tws.assistant.widget.ToggleButton;
import com.tencent.tws.assistant.widget.TwsButton;
import com.tencent.tws.framework.global.GlobalObj;
import com.tencent.tws.pay.PayNFCConstants;
import com.tencent.tws.phoneside.utils.DensityUtil;

import qrom.component.log.QRomLog;

public class ShowCardDetailsActivity extends TwsActivity
        implements OnWalletUICallback, OnClickListener {

    public static final String TAG = ShowCardDetailsActivity.class
            .getSimpleName();

    private Context mContext = null;

    private ICard mCard = null;

    private IOrder mOrder = null;

    private BaseCard mCardView = null;

    private TwsButton mSetDefaultButton = null;

    private TwsButton mChargeButton = null;

    private View mLoading = null;

    private ViewGroup mValidityLayout = null;

    private TextView mValidityTextView = null;

    private ViewGroup mCardNumLayout = null;

    private TextView mCardNumTextView = null;

    private ViewGroup mCardTranactLayout = null;

    private long mUniqueReq = -1;

    private static final int DIALOG_UBOUND = 1;

    private static final int DIALOG_UBOUND_CONFIRM = DIALOG_UBOUND + 1;

    private static final int DIALOG_SET_DEFAULT = DIALOG_UBOUND_CONFIRM + 1;

    private Runnable mClickRunable = new Runnable() {

        @Override
        public void run() {
            // TODO Auto-generated method stub
            PullUserInfo userInfo = new PullUserInfo();
            mUniqueReq = userInfo.getUniqueSeq();
            userInfo.invoke(new IResponseObserver() {

                @Override
                public void onResponseSucceed(long uniqueSeq, int operType, JceStruct response) {
                    // TODO Auto-generated method stub
                    if (uniqueSeq == mUniqueReq) {
                        GetCustomServiceRsp rsp = (GetCustomServiceRsp) response;
                        int ret = rsp.getIRet();
                        QRomLog.d(TAG, "onResponseSucceed: " + ret);
                        if (ret == 0) {
                            startCustomerSupportActivity(
                                    R.string.wallet_support_personal_info_has_submit);
                        } else {
                            startCustomerSupportActivity(R.string.wallet_support_summary);
                        }

                    }
                }

                @Override
                public void onResponseFailed(long uniqueSeq, int operType, int errorCode,
                        String description) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(),
                                    R.string.wallet_support_submit_fail,
                                    Toast.LENGTH_LONG).show();
                        }
                    });

                }
            });
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = this;

        Intent intent = getIntent();
        if (intent != null) {
            int type = intent.getIntExtra(
                    PayNFCConstants.ExtraKeyName.EXTRA_INT_CARDTYPE,
                    PayNFCConstants.Card.TYPE_UNKNOWN);
            String aid = intent
                    .getStringExtra(PayNFCConstants.ExtraKeyName.EXTRA_STR_INSTANCE_ID);
            mCard = CardManager.getInstance().getCard(aid);
            mOrder = OrderManager.getInstance().getLastOrder(aid);
        }

        if (mCard == null) {
            // TODO add tips
            finish();
            return;
        }

        ActionBar actionBar = getTwsActionBar();
        actionBar.setBackgroundDrawable(new ColorDrawable(getResources()
                .getColor(R.color.wallet_action_bar_background)));
        actionBar.setTitle(mCard.getCardName());

        switch (mCard.getCardType()) {
            case TRAFFIC_CARD:
                setContentView(R.layout.wallet_traffic_card_details);
                ToggleButton btn = (ToggleButton) actionBar.getMultiChoiceView(false);
                String support = getString(R.string.support);
                btn.setPadding(0, 0, DensityUtil.dip2px(this, 20), 0);
                btn.setText(support);
                btn.setTextOn(support);
                btn.setTextOff(support);
                btn.setOnClickListener(this);
                break;
            case BANK_CARD:
                // setContentView(R.layout.wallet_bank_card_details);
                break;
            default:
                return;
        }

        mCardView = (BaseCard) findViewById(R.id.wallet_card_detail_card);
        mCardView.hideWhenShowDetail(true);
        mCardView.attachCard(mCard);

        mValidityLayout = (ViewGroup) findViewById(R.id.wallet_traffic_card_validity_layout);
        mValidityTextView = (TextView) findViewById(R.id.wallet_traffic_card_validity_val);
        mCardNumLayout = (ViewGroup) findViewById(R.id.wallet_traffic_card_num_layout);
        mCardNumTextView = (TextView) findViewById(R.id.wallet_traffic_card_num_val);
        mCardTranactLayout = (ViewGroup) findViewById(R.id.wallet_traffic_card_transact_layout);
        mCardTranactLayout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                gotoTransactRecords();
            }
        });
        mSetDefaultButton = (TwsButton) findViewById(R.id.wallet_set_default_button);
        mSetDefaultButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!EnvManager.getInstance().isWatchConnected()) {
                    Toast.makeText(GlobalObj.g_appContext, R.string.wallet_disconnect_tips,
                            Toast.LENGTH_LONG).show();
                    return;
                }
                if (!CardManager.getInstance().isReady()) {
                    Toast.makeText(GlobalObj.g_appContext, R.string.wallet_sync_err_watch,
                            Toast.LENGTH_LONG).show();
                    return;
                }
                showTwsDialog(DIALOG_SET_DEFAULT);
            }
        });

        if (mCard.getCardType() == CARD_TYPE.TRAFFIC_CARD) {
            mChargeButton = (TwsButton) findViewById(R.id.wallet_traffic_card_charge);
            mChargeButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!EnvManager.getInstance().isWatchConnected()) {
                        Toast.makeText(GlobalObj.g_appContext, R.string.wallet_disconnect_tips,
                                Toast.LENGTH_LONG).show();
                        return;
                    }
                    charge();
                }
            });
        }
        TextView urlView = (TextView) findViewById(R.id.wallet_jump_url);
        if (CONFIG.BEIJINGTONG.mAID.equalsIgnoreCase(mCard.getAID())) {
            urlView.setVisibility(View.VISIBLE);
            urlView.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    jumpBeijingApp();
                }
            });
        }
        mLoading = findViewById(R.id.wallet_card_detail_loading_ly);
        hideLoading();
        WalletHandlerManager.getInstance().register(mCard.getAID(), ACTVITY_SCENE.SCENE_SWITCHCARD,
                this);
    }

    @Override
    public void onResume() {
        super.onResume();
        WalletHandlerManager.getInstance().requestFocus(ACTVITY_SCENE.SCENE_SWITCHCARD);
        reloadCard();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        WalletHandlerManager.getInstance().unregister(ACTVITY_SCENE.SCENE_SWITCHCARD);
        Utils.getWorkerHandler().removeCallbacks(mClickRunable);
        super.onDestroy();
    }

    private void setDefault() {
        showLoading();
        CardManager.getInstance().setDefaultCard(mCard.getAID());
    }

    private void showCard() {
        if (mCard.getActivationStatus() == ACTIVATION_STATUS.ACTIVATED) {
            mSetDefaultButton
                    .setText(getString(R.string.wallet_default_card_setted));
        } else {
            mSetDefaultButton
                    .setText(getString(R.string.wallet_set_default_card));
        }
        if (mCard.getActivationStatus() == ACTIVATION_STATUS.ACTIVATED
                || !EnvManager.getInstance().isWatchConnected()) {
            mSetDefaultButton.setEnabled(false);
        } else {
            mSetDefaultButton.setEnabled(true);
        }

        if (mCard.getCardType() == CARD_TYPE.TRAFFIC_CARD) {
            TextView balance = (TextView) findViewById(R.id.wallet_traffic_card_balance);
            balance.setText(String.format(
                    getString(R.string.wallet_traffic_card_balance),
                    Utils.getDisplayBalance(
                            ((ITrafficCard) mCard).getBalance())));
        }
        if (mOrder != null && mOrder.isCardTopFail()) {
            mChargeButton.setText(getString(R.string.charge_card_continue));
        } else {
            mChargeButton.setText(getString(R.string.charge_card_button));
        }
        // 每次对象不一样，需重新attachCard
        mCardView.attachCard(mCard);
        updateCardNumUI();
        updateValidityUI();
    }

    private void charge() {
        if (OrderManager.getInstanceInner().getPayConfig(mCard.getAID()) == null
                || !OrderManager.getInstance().isOrderReady()) {
            Toast.makeText(mContext, R.string.select_add_traffic_card_config_no_ready,
                    Toast.LENGTH_LONG).show();
            return;
        }
        if (!CardManager.getInstance().isReady()
                || !mCard.isReady()) {
            Toast.makeText(mContext, R.string.wallet_sync_err_watch,
                    Toast.LENGTH_LONG).show();
            return;
        }
        if (mOrder != null && mOrder.isCardTopFail()) {
            // 充值失败，点击继续充值 todo
            ShowLoadingActivity.launchLoading(
                    mContext,
                    mCard.getCardType(),
                    mCard.getAID(),
                    mOrder.getOrderReqParam()
                            .getEPayType(),
                    mOrder.getOrderReqParam()
                            .getITotalFee(),
                    ShowLoadingActivity.LOADING_TYPE_CHARGE_CARD,
                    true);
            return;
        }
        String today = Utils.getCurrentTime();
        String validity = ((ITrafficCard) mCard).getValidity();
        String startdate = ((ITrafficCard) mCard).getStartDate();
        if (!TextUtils.isEmpty(validity) && Utils.compareDate(today, validity) > 0) {
            // 超过有效期
            Toast.makeText(GlobalObj.g_appContext,
                    R.string.wallet_validity_tips,
                    Toast.LENGTH_LONG).show();
            return;
        }
        if (!TextUtils.isEmpty(startdate) && Utils.compareDate(today, startdate) < 0) {
            // 未到启用日期
            Toast.makeText(GlobalObj.g_appContext,
                    R.string.wallet_startdate_tips,
                    Toast.LENGTH_LONG).show();
            return;
        }
        Intent intent = new Intent(mContext, ChargeCardActivity.class);
        intent.putExtra(PayNFCConstants.ExtraKeyName.EXTRA_INT_CARDTYPE,
                mCard.getCardType());
        intent.putExtra(PayNFCConstants.ExtraKeyName.EXTRA_STR_INSTANCE_ID,
                mCard.getAID());
        mContext.startActivity(intent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        showTwsDialog(DIALOG_UBOUND);
        // showBottomDialog();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        // if (mCard.getCardType() == CARD_TYPE.BANK_CARD) {
        // getMenuInflater().inflate(
        // R.menu.wallet_card_detail_action_bar_menu, menu);
        // }

        return true;
    }

    @Override
    protected TwsDialog onCreateTwsDialog(int id) {
        TwsDialog dialog = null;
        switch (id) {
            case DIALOG_UBOUND:
                dialog = new AlertDialog.Builder(mContext, true)
                        .setTitle(getString(R.string.wallet_ubound_dialog_title))
                        .setBottomButtonItems(R.array.wallet_card_detail_unbound,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface arg0,
                                            int which) {
                                        switch (which) {
                                            case 0:
                                                showTwsDialog(DIALOG_UBOUND_CONFIRM);
                                                break;
                                            case 1:
                                                break;
                                        }
                                    }
                                })
                        .setBottomButtonColorItems(
                                new int[] {
                                        AlertDialog.BOTTOM_BUTTON_COLOR_RED,
                                        Color.WHITE
                                }).create(true);
                dialog.setTitleTextSize(getResources().getDimension(
                        R.dimen.wallet_dialog_title_text_size)
                        / getResources().getDisplayMetrics().density);
                dialog.setTitleTextColor(getResources().getColor(
                        R.color.wallet_dialog_title_text_color));
                break;
            case DIALOG_UBOUND_CONFIRM:
                dialog = new AlertDialog.Builder(mContext)
                        .setMessage(R.string.wallet_ubound_dialog_message)
                        .setPositiveButton(R.string.wallet_ubound_dialog_positive,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                            int whichButton) {
                                        finish();
                                    }
                                })
                        .setNegativeButton(R.string.wallet_ubound_dialog_negative,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                            int whichButton) {
                                    }
                                })
                        .create();
                break;
            case DIALOG_SET_DEFAULT:
                dialog = new AlertDialog.Builder(mContext, true)
                        .setTitle(
                                String.format(
                                        getString(R.string.wallet_set_default_title),
                                        mCard.getCardName()))
                        .setBottomButtonItems(
                                R.array.wallet_card_detail_set_default,
                                new DialogInterface.OnClickListener() {

                                    @Override
                                    public void onClick(DialogInterface arg0,
                                            int which) {
                                        switch (which) {
                                            case 0:
                                                setDefault();
                                                break;
                                            case 1:
                                                break;
                                        }
                                    }
                                })
                        .setBottomButtonColorItems(
                                new int[] {
                                        AlertDialog.BOTTOM_BUTTON_COLOR_RED,
                                        Color.WHITE
                                }).create(true);

                dialog.setTitleTextSize(getResources().getDimension(
                        R.dimen.wallet_dialog_title_text_size)
                        / getResources().getDisplayMetrics().density);
                dialog.setTitleTextColor(getResources().getColor(
                        R.color.wallet_dialog_title_text_color));
                break;
        }
        return dialog;
    }

    private void showLoading() {
        mLoading.setVisibility(View.VISIBLE);
    }

    private void hideLoading() {
        mLoading.setVisibility(View.GONE);
    }

    private void reloadCard() {
        mCard = CardManager.getInstance().getCard(mCard.getAID());
        mOrder = OrderManager.getInstance().getLastOrder(mCard.getAID());
        showCard();
    }

    @Override
    public void onUIUpdate(MODULE_CALLBACK module, final int ret, final boolean forUpdateUI) {
        this.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                if (mLoading.getVisibility() == View.VISIBLE && ret != 0) {
                    Toast.makeText(mContext, R.string.wallet_set_default_dev_not_connected,
                            Toast.LENGTH_LONG).show();
                }
                reloadCard();
                hideLoading();
            }
        });

    }

    @Override
    public void onClick(View arg0) {
        // boolean hasClicked = Utils.getWorkerHandler().hasCallbacks(mClickRunable);
        // if (!hasClicked) {
        // Utils.getWorkerHandler().post(mClickRunable);
        // }

    }

    private void startCustomerSupportActivity(int resid) {
        Intent intent = new Intent(this, CustomerSupportActivity.class);
        intent.putExtra("AID", mCard.getAID());
        intent.putExtra("tittle_resid", resid);
        startActivity(intent);
    }

    private void updateValidityUI() {
        String validty = ((ITrafficCard) mCard).getValidity();
        if (TextUtils.isEmpty(validty)) {
            mValidityLayout.setVisibility(View.GONE);
        } else {
            mValidityLayout.setVisibility(View.VISIBLE);
        }
        mValidityTextView.setText(((ITrafficCard) mCard).getValidity());
    }

    private void updateCardNumUI() {
        String cardNum = ((ITrafficCard) mCard).getCardNumber();
        if (TextUtils.isEmpty(cardNum)) {
            mCardNumLayout.setVisibility(View.GONE);
        } else {
            mCardNumLayout.setVisibility(View.VISIBLE);
        }
        mCardNumTextView.setText(cardNum);
    }

    private void gotoTransactRecords() {
        Intent intent = new Intent();
        intent.setClass(ShowCardDetailsActivity.this, CardTransactActivity.class);
        intent.putExtra(PayNFCConstants.ExtraKeyName.EXTRA_STR_INSTANCE_ID, mCard.getAID());
        startActivity(intent);
    }

    private void jumpBeijingApp() {
        Uri uri = Uri.parse("http://a.app.qq.com/o/simple.jsp?pkgname=cn.com.bmac.nfc");
        Intent it = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(it);
    }
}
