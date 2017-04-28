
package com.pacewear.tws.phoneside.wallet.ui2.activity;

import android.app.Activity;
import android.app.TwsActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.pacewear.tws.phoneside.wallet.R;
import com.pacewear.tws.phoneside.wallet.WalletApp;
import com.pacewear.tws.phoneside.wallet.card.CardManager;
import com.pacewear.tws.phoneside.wallet.card.ICard;
import com.pacewear.tws.phoneside.wallet.card.ITrafficCard;
import com.pacewear.tws.phoneside.wallet.card.ICard.ACTIVATION_STATUS;
import com.pacewear.tws.phoneside.wallet.card.ICard.CARD_TYPE;
import com.pacewear.tws.phoneside.wallet.card.ICardInner.CONFIG;
import com.pacewear.tws.phoneside.wallet.common.ClickFilter;
import com.pacewear.tws.phoneside.wallet.common.UIHelper;
import com.pacewear.tws.phoneside.wallet.common.Utils;
import com.pacewear.tws.phoneside.wallet.env.EnvManager;
import com.pacewear.tws.phoneside.wallet.lnt.ILntInvokeCallback;
import com.pacewear.tws.phoneside.wallet.lnt.ILntSdk;
import com.pacewear.tws.phoneside.wallet.lnt.LntSdk;
import com.pacewear.tws.phoneside.wallet.lnt.ILntSdk.ILntCardPage;
import com.pacewear.tws.phoneside.wallet.order.IOrder;
import com.pacewear.tws.phoneside.wallet.order.OrderManager;
import com.pacewear.tws.phoneside.wallet.ui.CardTransactActivity;
import com.pacewear.tws.phoneside.wallet.ui.ChargeCardActivity;
import com.pacewear.tws.phoneside.wallet.ui.ShowCardDetailsActivity;
import com.pacewear.tws.phoneside.wallet.ui.ShowLoadingActivity;
import com.pacewear.tws.phoneside.wallet.ui.handler.WalletHandlerManager;
import com.pacewear.tws.phoneside.wallet.ui.handler.WalletBaseHandler.ACTVITY_SCENE;
import com.pacewear.tws.phoneside.wallet.ui.handler.WalletBaseHandler.MODULE_CALLBACK;
import com.pacewear.tws.phoneside.wallet.ui.handler.WalletBaseHandler.OnWalletUICallback;
import com.pacewear.tws.phoneside.wallet.ui.widget.BaseCard;
import com.pacewear.tws.phoneside.wallet.ui.widget.TwsDialogController;
import com.pacewear.tws.phoneside.wallet.ui.widget.TwsDialogController.OnItemEvent;
import com.pacewear.tws.phoneside.wallet.ui2.toast.WalletErrToast;
import com.tencent.tws.assistant.app.ActionBar;
import com.tencent.tws.assistant.app.AlertDialog;
import com.tencent.tws.assistant.app.TwsDialog;
import com.tencent.tws.assistant.widget.Toast;
import com.tencent.tws.assistant.widget.TwsButton;
import com.tencent.tws.pay.PayNFCConstants;
import com.tencent.tws.phoneside.phoneverify.PhoneVerifyActivity;

public class TrafficCardDetailActivity extends TwsActivity
        implements OnWalletUICallback, OnClickListener {

    public static final String TAG = TrafficCardDetailActivity.class
            .getSimpleName();

    private Context mContext = null;

    private ICard mCard = null;

    private IOrder mOrder = null;

    private BaseCard mCardView = null;

    private TwsButton mChargeButton = null;

    private View mLoading = null;

    private ViewGroup mValidityLayout = null;

    private TextView mValidityTextView = null;

    private ViewGroup mCardNumLayout = null;

    private TextView mCardNumTextView = null;

    private ViewGroup mCardTranactLayout = null;

    private static final int DIALOG_MORE_OPTION = 1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = this;

        Intent intent = getIntent();
        if (intent != null) {
            String aid = intent
                    .getStringExtra(PayNFCConstants.ExtraKeyName.EXTRA_STR_INSTANCE_ID);
            mCard = CardManager.getInstance().getCard(aid);
            mOrder = OrderManager.getInstance().getLastOrder(aid);
        }

        if (mCard == null) {
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
                justMoreOptionBtn(actionBar);
                break;
            case BANK_CARD:
                // setContentView(R.layout.wallet_bank_card_details);
            default:
                break;
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

        if (mCard.getCardType() == CARD_TYPE.TRAFFIC_CARD) {
            mChargeButton = (TwsButton) findViewById(R.id.wallet_traffic_card_charge);
            UIHelper.setTwsButton(mChargeButton, R.string.charge_card_button, 14);
            mChargeButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (ClickFilter.isMultiClick()) {
                        return;
                    }
                    if (!EnvManager.getInstance().isWatchConnected()) {
                        Toast.makeText(WalletApp.getHostAppContext(),
                                getString(R.string.wallet_disconnect_tips),
                                Toast.LENGTH_LONG).show();
                        return;
                    }
                    charge();
                }
            });
        }
        TextView urlView = (TextView) findViewById(R.id.wallet_jump_url);
        TextView bjtTipsView = (TextView) findViewById(R.id.wallet_bjt_asssit_tip);
        if (CONFIG.BEIJINGTONG.mAID.equalsIgnoreCase(mCard.getAID())) {
            urlView.setVisibility(View.VISIBLE);
            bjtTipsView.setVisibility(View.VISIBLE);
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
        super.onDestroy();
    }

    private void showCard() {
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
        if (WalletErrToast.checkAll(this)) {
            return;
        }
        if (mOrder != null && mOrder.isCardTopFail()) {
            // 充值失败，点击继续充值 todo
            ShowLoadingActivity.launchLoading(
                    mContext,
                    mCard.getCardType(),
                    mCard.getAID(),
                    mOrder.getOrderReqParam().getEPayScene(),
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
            Toast.makeText(WalletApp.getHostAppContext(),
                    getString(R.string.wallet_validity_tips),
                    Toast.LENGTH_LONG).show();
            return;
        }
        if (!TextUtils.isEmpty(startdate) && Utils.compareDate(today, startdate) < 0) {
            // 未到启用日期
            Toast.makeText(WalletApp.getHostAppContext(),
                    getString(R.string.wallet_startdate_tips),
                    Toast.LENGTH_LONG).show();
            return;
        }
        Intent intent = new Intent(mContext, ChargeCardActivity.class);
        intent.putExtra(PayNFCConstants.ExtraKeyName.EXTRA_INT_CARDTYPE,
                mCard.getCardType().toValue());
        intent.putExtra(PayNFCConstants.ExtraKeyName.EXTRA_STR_INSTANCE_ID,
                mCard.getAID());
        mContext.startActivity(intent);
    }

    @Override
    protected TwsDialog onCreateTwsDialog(int id) {
        TwsDialog dialog = null;
        switch (id) {
            case DIALOG_MORE_OPTION:
                dialog = new TwsDialogController(this, true)
                        .fillItems(R.array.wallet_card_detail_more, new OnItemEvent[] {
                                mLntComplaint, mLntComplaintQuery
                }).flush();
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
                    Toast.makeText(WalletApp.getHostAppContext(),
                            getString(R.string.wallet_set_default_dev_not_connected),
                            Toast.LENGTH_LONG).show();
                }
                reloadCard();
                hideLoading();
            }
        });

    }

    @Override
    public void onClick(View arg0) {
        if (ClickFilter.isMultiClick()) {
            return;
        }
        if (hasMoreOption()) {
            showTwsDialog(DIALOG_MORE_OPTION);
        }
    }

    private boolean hasMoreOption() {
        if (CONFIG.LINGNANTONG.mAID.equalsIgnoreCase(mCard.getAID())) {
            return true;
        }
        return false;
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
        intent.setClass(TrafficCardDetailActivity.this, CardTransactActivity.class);
        intent.putExtra(PayNFCConstants.ExtraKeyName.EXTRA_STR_INSTANCE_ID, mCard.getAID());
        startActivity(intent);
    }

    private void jumpBeijingApp() {
        Uri uri = Uri.parse("http://a.app.qq.com/o/simple.jsp?pkgname=cn.com.bmac.nfc");
        Intent it = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(it);
    }

    private void justMoreOptionBtn(ActionBar actionBar) {
        ImageView btn = (ImageView) actionBar.getRightButtonView();
        btn.setImageResource(R.drawable.wallet_more_option);
        btn.setOnClickListener(this);
        if (!hasMoreOption()) {
            btn.setVisibility(View.GONE);
        }
    }
}
