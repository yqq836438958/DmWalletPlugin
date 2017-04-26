
package com.pacewear.tws.phoneside.wallet.ui2.activity;

import android.app.TwsActivity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.pacewear.tws.phoneside.wallet.R;
import com.pacewear.tws.phoneside.wallet.WalletApp;
import com.pacewear.tws.phoneside.wallet.card.CardManager;
import com.pacewear.tws.phoneside.wallet.card.ICard;
import com.pacewear.tws.phoneside.wallet.card.ITrafficCard;
import com.pacewear.tws.phoneside.wallet.card.ICard.CARD_TYPE;
import com.pacewear.tws.phoneside.wallet.common.Utils;
import com.pacewear.tws.phoneside.wallet.order.OrderManager;
import com.pacewear.tws.phoneside.wallet.ui.ChargeCardActivity;
import com.pacewear.tws.phoneside.wallet.ui.ShowLoadingActivity;
import com.pacewear.tws.phoneside.wallet.ui.widget.PayValueSelect;
import com.pacewear.tws.phoneside.wallet.ui.widget.PayValueSelect.OnSelectChangeListener;
import com.pacewear.tws.phoneside.wallet.ui2.toast.WalletErrToast;
import com.qq.taf.jce.JceStruct;
import com.tencent.tws.assistant.app.ActionBar;
import com.tencent.tws.assistant.widget.Toast;
import com.tencent.tws.assistant.widget.TwsButton;
import com.tencent.tws.pay.PayNFCConstants;

import java.util.ArrayList;

import TRom.E_PAY_SCENE;
import TRom.E_PAY_TYPE;
import TRom.PayConfig;
import TRom.PayRechargeAmount;
import qrom.component.log.QRomLog;

public class CardTopupPrepareActivity extends TwsActivity {

    public static final String TAG = ChargeCardActivity.class.getSimpleName();

    private ICard mCard = null;

    // 支付配置
    private PayConfig mPayConfig = null;

    // 充值配置
    ArrayList<PayRechargeAmount> mPayRechargeAmount = null;

    private Context mContext = null;

    private PayValueSelect mPayValueSelect = null;

    private static final int PAY_CHANNEL_WECHAT = 0;

    private static final int PAY_CHANNEL_QQ = PAY_CHANNEL_WECHAT + 1;

    private int mPayChannelSelected = PAY_CHANNEL_WECHAT;

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
        actionBar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(
                R.color.wallet_action_bar_background)));
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

        setContentView(R.layout.wallet_charge_card);

        TwsButton confirm = (TwsButton) findViewById(R.id.wallet_charge_confirm);
        confirm.setButtonMode(TwsButton.RecommendedButton);
        confirm.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (WalletErrToast.checkAll(CardTopupPrepareActivity.this)) {
                    return;
                }
                int payType = mPayChannelSelected == PAY_CHANNEL_QQ ? E_PAY_TYPE._E_PT_QQ_PAY
                        : E_PAY_TYPE._E_PT_WEIXIN_PAY;
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
                ShowLoadingActivity.launchLoading(mContext, mCard.getCardType(), mCard.getAID(),
                        E_PAY_SCENE._EPS_STAT, payType,
                        chargeValue, ShowLoadingActivity.LOADING_TYPE_CHARGE_CARD, false);
                finish();
            }
        });

        mPayValueSelect = (PayValueSelect) findViewById(R.id.pay_value_select);
        mPayValueSelect.setPayRechargeAmount(mPayRechargeAmount);
        mPayValueSelect.setSelect(OnSelectChangeListener.RIGHT_SELECTED);

    }
}
