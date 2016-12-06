
package com.pacewear.tws.phoneside.wallet.ui;

import TRom.E_PAY_TYPE;
import TRom.PayConfig;
import TRom.PayRechargeAmount;
import qrom.component.log.QRomLog;

import android.app.TwsActivity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;

import com.qq.taf.jce.JceStruct;
import com.tencent.tws.assistant.app.ActionBar;
import com.tencent.tws.assistant.widget.AdapterView;
import com.tencent.tws.assistant.widget.AdapterView.OnItemClickListener;
import com.tencent.tws.framework.global.GlobalObj;
import com.tencent.tws.assistant.widget.ListView;
import com.tencent.tws.assistant.widget.Toast;
import com.tencent.tws.assistant.widget.TwsButton;
import com.tencent.tws.gdevicemanager.R;
import com.tencent.tws.pay.PayNFCConstants;
import java.util.ArrayList;

public class ChargeCardActivity extends TwsActivity {

    public static final String TAG = ChargeCardActivity.class.getSimpleName();

    private ICard mCard = null;

    // 支付配置
    private PayConfig mPayConfig = null;

    // 充值配置
    ArrayList<PayRechargeAmount> mPayRechargeAmount = null;

    private Context mContext = null;

    private PayValueSelect mPayValueSelect = null;

    private BaseAdapter mListAdapter = null;

    private static final int PAY_CHANNEL_WECHAT = 0;

    private static final int PAY_CHANNEL_QQ = PAY_CHANNEL_WECHAT + 1;

    private int mPayChannelSelected = PAY_CHANNEL_WECHAT;

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, R.anim.wallet_push_down);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        overridePendingTransition(R.anim.wallet_push_up, 0);

        super.onCreate(savedInstanceState);

        mContext = this;

        Intent intent = getIntent();
        if (intent != null) {
            int type = intent.getIntExtra(
                    PayNFCConstants.ExtraKeyName.EXTRA_INT_CARDTYPE,
                    PayNFCConstants.Card.TYPE_UNKNOWN);
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
        confirm.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!EnvManager.getInstance().isWatchConnected()) {
                    Toast.makeText(GlobalObj.g_appContext, R.string.wallet_disconnect_tips,
                            Toast.LENGTH_LONG).show();
                    return;
                }
                if (!CardManager.getInstance().isReady()
                        || !mCard.isReady()) {
                    Toast.makeText(mContext, R.string.wallet_sync_err_watch,
                            Toast.LENGTH_LONG).show();
                    return;
                }
                int payType = mPayChannelSelected == PAY_CHANNEL_QQ ? E_PAY_TYPE._E_PT_QQ_PAY
                        : E_PAY_TYPE._E_PT_WEIXIN_PAY;
                if (!PayManager.isPayChannelSupport(payType)) {
                    Toast.makeText(GlobalObj.g_appContext,
                            (payType == PAY_CHANNEL_QQ) ? R.string.login_download_qq
                                    : R.string.login_download_mm,
                            Toast.LENGTH_LONG).show();
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
                        Toast.makeText(GlobalObj.g_appContext,
                                R.string.wallet_sync_err_watch,
                                Toast.LENGTH_LONG).show();
                        return;
                    }
                    long iCurBalance = Long.parseLong(strBalance);
                    if (iMaxRechargeAmount > chargeValue && (chargeValue
                            + iCurBalance) > iMaxRechargeAmount) {
                        Toast.makeText(GlobalObj.g_appContext,
                                String.format(getString(R.string.wallet_flow_charge_amount),
                                        Utils.getDisplayBalance(iMaxRechargeAmount)),
                                Toast.LENGTH_LONG).show();
                        return;
                    }
                    if ((chargeValue + iCurBalance) <= 0) {
                        // 透支判断
                        Toast.makeText(GlobalObj.g_appContext,
                                R.string.wallet_amount_nozero,
                                Toast.LENGTH_LONG).show();
                        return;
                    }
                }
                ShowLoadingActivity.launchLoading(mContext, mCard.getCardType(), mCard.getAID(),
                        payType,
                        chargeValue, ShowLoadingActivity.LOADING_TYPE_CHARGE_CARD, false);
                finish();
            }
        });

        mListAdapter = new BaseAdapter() {

            @Override
            public int getCount() {
                return 2;
            }

            @Override
            public Object getItem(int position) {
                return position;
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {

                SimpleCardListItem item = null;

                if (convertView == null) {
                    convertView = new SimpleCardListItem(mContext);
                }

                item = (SimpleCardListItem) convertView;

                switch (position) {
                    case PAY_CHANNEL_WECHAT:
                        item.setIcon(R.drawable.wallet_pay_channel_wechat);
                        item.setDescription(R.string.wallet_pay_channel_wechat);
                        break;
                    case PAY_CHANNEL_QQ:
                        item.setIcon(R.drawable.wallet_pay_channel_qq);
                        item.setDescription(R.string.wallet_pay_channel_qq);
                        break;
                }

                if (mPayChannelSelected == position) {
                    item.setItemSelect(true);
                } else {
                    item.setItemSelect(false);
                }

                return item;
            }

        };

        mPayValueSelect = (PayValueSelect) findViewById(R.id.pay_value_select);
        mPayValueSelect.setPayRechargeAmount(mPayRechargeAmount);
        mPayValueSelect.setSelect(OnSelectChangeListener.RIGHT_SELECTED);

        ListView payChannelList = (ListView) findViewById(R.id.pay_channel_list);
        payChannelList.setAdapter(mListAdapter);

        payChannelList.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1,
                    int position, long arg3) {
                mPayChannelSelected = position;
                mListAdapter.notifyDataSetChanged();
            }
        });

    }
}
