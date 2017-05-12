
package com.pacewear.tws.phoneside.wallet.ui2.activity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.pacewear.tws.phoneside.wallet.R;
import com.pacewear.tws.phoneside.wallet.WalletApp;
import com.pacewear.tws.phoneside.wallet.bean.OrderBean;
import com.pacewear.tws.phoneside.wallet.card.CardManager;
import com.pacewear.tws.phoneside.wallet.card.ICard;
import com.pacewear.tws.phoneside.wallet.card.ITrafficCard;
import com.pacewear.tws.phoneside.wallet.card.ICard.CARD_TYPE;
import com.pacewear.tws.phoneside.wallet.card.ICardInner.CONFIG;
import com.pacewear.tws.phoneside.wallet.common.ClickFilter;
import com.pacewear.tws.phoneside.wallet.common.Utils;
import com.pacewear.tws.phoneside.wallet.lnt.ILntInvokeCallback;
import com.pacewear.tws.phoneside.wallet.lnt.ILntSdk;
import com.pacewear.tws.phoneside.wallet.lnt.LntSdk;
import com.pacewear.tws.phoneside.wallet.order.IOrder;
import com.pacewear.tws.phoneside.wallet.order.OrderManager;
import com.pacewear.tws.phoneside.wallet.ui.handler.WalletHandlerManager;
import com.pacewear.tws.phoneside.wallet.ui.widget.TwsDialogController;
import com.pacewear.tws.phoneside.wallet.ui.handler.WalletBaseHandler.ACTVITY_SCENE;
import com.pacewear.tws.phoneside.wallet.ui.handler.WalletBaseHandler.MODULE_CALLBACK;
import com.pacewear.tws.phoneside.wallet.ui.handler.WalletBaseHandler.OnWalletUICallback;
import com.pacewear.tws.phoneside.wallet.ui2.toast.WalletErrToast;
import com.pacewear.tws.phoneside.wallet.ui2.widget.BaseCardView;
import com.pacewear.tws.phoneside.wallet.ui.widget.TwsDialogController.OnItemEvent;
import com.tencent.tws.assistant.app.TwsDialog;
import com.tencent.tws.assistant.widget.Toast;
import com.tencent.tws.pay.PayNFCConstants;

public class TrafficCardDetailActivity extends TwsWalletActivity
        implements OnWalletUICallback {

    public static final String TAG = TrafficCardDetailActivity.class
            .getSimpleName();

    private Context mContext = null;

    private ICard mCard = null;

    private IOrder mOrder = null;

    private BaseCardView mCardView = null;

    private Button mChargeButton = null;

    private View mLoading = null;

    private ViewGroup mValidityLayout = null;

    private TextView mValidityTextView = null;

    private ViewGroup mCardNumLayout = null;

    private TextView mCardNumTextView = null;

    private ViewGroup mCardTranactLayout = null;

    private static final int DIALOG_MORE_OPTION = 1;

    private boolean mIsLingNanTongPage = false;
    private ILntSdk mLntSdk = null;
    private ILntInvokeCallback mLntInvokeCallback = new ILntInvokeCallback() {
        @Override
        public void onResult(boolean suc, final String desc) {
            if (suc) {
                showLoading();
                mCard.forceUpdate();
            } else {
                TrafficCardDetailActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WalletApp.getHostAppContext(), desc,
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    };
    private OnItemEvent mLntComplaint = new OnItemEvent() {

        @Override
        public void onHandle() {
            Toast.makeText(WalletApp.getHostAppContext(), "complaint for lnt", Toast.LENGTH_LONG)
                    .show();
            if (mLntSdk != null) {
                mLntSdk.complaint(TrafficCardDetailActivity.this);
            }
        }
    };
    private OnItemEvent mQA = new OnItemEvent() {

        @Override
        public void onHandle() {
            gotoHelpPage();
        }
    };
    private OnItemEvent mLntComplaintQuery = new OnItemEvent() {

        @Override
        public void onHandle() {
            Toast.makeText(WalletApp.getHostAppContext(), "query complaint for lnt",
                    Toast.LENGTH_LONG)
                    .show();
            if (mLntSdk != null) {
                mLntSdk.complaintQuery(TrafficCardDetailActivity.this);
            }
        }
    };

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
        mIsLingNanTongPage = CONFIG.LINGNANTONG.mAID.equalsIgnoreCase(mCard.getAID());
        if (mIsLingNanTongPage) {
            setActionBar(mCard.getCardName(), new RightMoreOptionStagy());
        } else {
            setActionBar(mCard.getCardName(), new RightHelpStagy());
        }

        switch (mCard.getCardType()) {
            case TRAFFIC_CARD:
                setContentView(R.layout.wallet2_traffic_card_details);
                break;
            case BANK_CARD:
                // setContentView(R.layout.wallet_bank_card_details);
            default:
                break;
        }

        mCardView = (BaseCardView) findViewById(R.id.wallet_card_detail_card);
        mCardView.attachCard(mCard, BaseCardView.SENCE_SINGLE);

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
            mChargeButton = (Button) findViewById(R.id.wallet_traffic_card_charge);
            mChargeButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (ClickFilter.isMultiClick()
                            || WalletErrToast.checkAll(TrafficCardDetailActivity.this)) {
                        return;
                    }
                    charge();
                }
            });
        }
        TextView bjtTipsView = (TextView) findViewById(R.id.wallet_bjt_asssit_tip);
        if (CONFIG.BEIJINGTONG.mAID.equalsIgnoreCase(mCard.getAID())) {
            bjtTipsView.setVisibility(View.VISIBLE);
            bjtTipsView.setOnClickListener(new OnClickListener() {

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
        if (mIsLingNanTongPage) {
            mLntSdk = LntSdk.getInstance();
        }
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
        if (mLntSdk != null) {
            mLntSdk.clear();
        }
        super.onDestroy();
    }

    private void showCard() {
        if (mOrder != null && mOrder.isCardTopFail() && !mOrder.isInRefunding()) {
            mChargeButton.setText(getString(R.string.charge_card_continue));
        } else {
            mChargeButton.setText(getString(R.string.charge_card_button));
        }
        // 每次对象不一样，需重新attachCard
        mCardView.attachCard(mCard, BaseCardView.SENCE_SINGLE);
        updateCardNumUI();
        updateValidityUI();
    }

    private void continueLastCharge() {
        Intent intent = new Intent(mContext, BusinessLoadingActivity.class);
        intent.putExtra(BusinessLoadingActivity.KEY_ORDER_BEAN,
                OrderBean.genByLastOrder(mCard.getAID(), mOrder));
        mContext.startActivity(intent);
    }

    private void charge() {
        if (WalletErrToast.checkAll(this)) {
            return;
        }
        if (mLntSdk != null && mLntSdk.charge(this, mLntInvokeCallback)) {
            return;
        }
        if (mOrder != null && mOrder.isCardTopFail()) {
            if (mOrder.isInRefunding()) {
                Toast.makeText(WalletApp.getHostAppContext(),
                        getString(R.string.wallet_refund_click_tip), Toast.LENGTH_LONG).show();
            } else {
                continueLastCharge();
            }
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
        Intent intent = new Intent(mContext, CardTopupPrepareActivity.class);
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
                                mLntComplaint, mLntComplaintQuery, mQA
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

    @Override
    protected void onMoreOptionClick() {
        super.onMoreOptionClick();
        showTwsDialog(DIALOG_MORE_OPTION);
    }
}
