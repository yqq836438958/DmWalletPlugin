
package com.pacewear.tws.phoneside.wallet.ui2.activity;

import android.app.Activity;
import android.app.TwsActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.pacewear.tws.phoneside.wallet.R;
import com.pacewear.tws.phoneside.wallet.WalletApp;
import com.pacewear.tws.phoneside.wallet.bean.OrderBean;
import com.pacewear.tws.phoneside.wallet.card.CardManager;
import com.pacewear.tws.phoneside.wallet.card.ICard;
import com.pacewear.tws.phoneside.wallet.card.ITrafficCard;
import com.pacewear.tws.phoneside.wallet.card.ICard.CARD_TYPE;
import com.pacewear.tws.phoneside.wallet.common.Utils;
import com.pacewear.tws.phoneside.wallet.order.OrderManager;
import com.pacewear.tws.phoneside.wallet.ui2.toast.WalletErrToast;
import com.pacewear.tws.phoneside.wallet.ui2.widget.BaseCardView;
import com.pacewear.tws.phoneside.wallet.ui2.widget.PayValueSelect;
import com.pacewear.tws.phoneside.wallet.ui2.widget.PayValueSelect.OnSelectChangeListener;
import com.pacewear.tws.phoneside.wallet.ui2.widget.TrafficCardView;
import com.qq.taf.jce.JceStruct;
import com.tencent.tws.assistant.app.ActionBar;
import com.tencent.tws.assistant.widget.Toast;
import com.tencent.tws.pay.PayNFCConstants;

import java.util.ArrayList;

import TRom.E_PAY_SCENE;
import TRom.E_PAY_TYPE;
import TRom.PayConfig;
import TRom.PayRechargeAmount;
import qrom.component.log.QRomLog;

public class CardTopupPrepareActivity extends TwsActivity {

    public static final String TAG = CardTopupPrepareActivity.class.getSimpleName();

    private ICard mCard = null;

    // 支付配置
    private PayConfig mPayConfig = null;

    // 充值配置
    ArrayList<PayRechargeAmount> mPayRechargeAmount = null;

    private PayValueSelect mPayValueSelect = null;

    @Override
    public void finish() {
        super.finish();
        // overridePendingTransition(0, R.anim.wallet_push_down);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        // overridePendingTransition(R.anim.wallet_push_up, 0);

        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        if (intent != null) {
            String instanceId = intent
                    .getStringExtra(PayNFCConstants.ExtraKeyName.EXTRA_STR_INSTANCE_ID);
            mCard = CardManager.getInstance().getCard(instanceId);
            mPayConfig = OrderManager.getInstanceInner().getPayConfig(instanceId);
        }

        if (mCard == null) {
            finish();
            return;
        }

        // TODO
        // 没有该卡的支付配置信息
        if (mPayConfig == null) {
            finish();
            return;
        }
        QRomLog.d(TAG, "mPayConfig: " + JceStruct.toDisplaySimpleString(mPayConfig));

        // 开卡费 这命名简直是充值费！
        // TODO 优惠活动、确保开卡费合法
        mPayRechargeAmount = mPayConfig.getVPayRechargeAmountList();

        // TODO
        if (mPayRechargeAmount == null || mPayRechargeAmount.size() < 3) {
            finish();
            return;
        }

        ActionBar actionBar = getTwsActionBar();
        actionBar.setTitle(getString(R.string.charge_card_title, mCard.getCardName()));

        Button actionLeftBt = (Button) actionBar.getCloseView(false);
        actionLeftBt.setText(getResources().getString(
                R.string.wallet_select_default_cancel));
        actionLeftBt.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                finish();
            }
        });

        setContentView(R.layout.wallet2_activity_cardtopup);
        TrafficCardView cardView = (TrafficCardView) findViewById(R.id.wallet_topup_card_panel);
        cardView.attachCard(mCard, BaseCardView.SENCE_SINGLE);
        Button confirm = (Button) findViewById(R.id.wallet_charge_confirm);
        confirm.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (WalletErrToast.checkAll(CardTopupPrepareActivity.this)) {
                    return;
                }
                long chargeValue = 0;
                int selected = mPayValueSelect.getSelected();
                switch (selected) {
                    case OnSelectChangeListener.LEFT_SELECTED:
                        chargeValue = mPayRechargeAmount.get(0).getITotalFee();
                        break;
                    case OnSelectChangeListener.MIDDLE_SELECTED:
                        chargeValue = mPayRechargeAmount.get(1).getITotalFee();
                        break;
                    case OnSelectChangeListener.RIGHT_SELECTED:
                        chargeValue = mPayRechargeAmount.get(2).getITotalFee();
                        break;
                }
                if (mCard.getCardType() == CARD_TYPE.TRAFFIC_CARD) {
                    long iMaxRechargeAmount = mPayConfig
                            .getIMaxRechargeAmount();
                    String strBalance = ((ITrafficCard) mCard).getBalance();
                    if (TextUtils.isEmpty(strBalance)) {
                        Toast.makeText(WalletApp.getHostAppContext(),
                                getString(R.string.wallet_sync_err_watch),
                                Toast.LENGTH_LONG).show();
                        return;
                    }
                    long iCurBalance = Long.parseLong(strBalance);
                    if (iMaxRechargeAmount > chargeValue && (chargeValue
                            + iCurBalance) > iMaxRechargeAmount) {
                        Toast.makeText(WalletApp.getHostAppContext(),
                                String.format(
                                        getString(R.string.wallet_flow_charge_amount),
                                        Utils.getDisplayBalance(iMaxRechargeAmount)),
                                Toast.LENGTH_LONG).show();
                        return;
                    }
                    if ((chargeValue + iCurBalance) <= 0) {
                        // 透支判断
                        Toast.makeText(WalletApp.getHostAppContext(),
                                getString(R.string.wallet_amount_nozero),
                                Toast.LENGTH_LONG).show();
                        return;
                    }
                }
                goPayChoosePage(chargeValue);
            }
        });

        mPayValueSelect = (PayValueSelect) findViewById(R.id.pay_value_select);
        mPayValueSelect.setPayRechargeAmount(mPayRechargeAmount);
        mPayValueSelect.setSelect(OnSelectChangeListener.RIGHT_SELECTED);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
            Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            goTopupPageInternal(data.getIntExtra(PayChooseActivity.PAY_TYPE, 0),
                    data.getLongExtra(PayChooseActivity.PAY_AMOUNT, 0L));
        }
    }

    private void goTopupPageInternal(int payType, long chargeFee) {
        Intent intent = new Intent(this, BusinessLoadingActivity.class);
        OrderBean bean = OrderBean.genNewInstance(mCard.getAID(), payType, E_PAY_SCENE._EPS_STAT, 0,
                chargeFee, false);
        intent.putExtra(BusinessLoadingActivity.KEY_ORDER_BEAN, bean);
        startActivity(intent);
        finish();
    }

    private void goPayChoosePage(long chargeVal) {
        Intent intent = new Intent(this, PayChooseActivity.class);
        intent.putExtra(PayChooseActivity.PAY_AMOUNT, chargeVal);
        intent.putExtra(PayChooseActivity.PAY_DESC, mCard.getCardName());
        startActivityForResult(intent, 1);
    }

}
