
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
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;

import com.pacewear.tws.phoneside.wallet.R;
import com.pacewear.tws.phoneside.wallet.card.CardManager;
import com.pacewear.tws.phoneside.wallet.card.ICard;
import com.pacewear.tws.phoneside.wallet.card.ICardInner.CONFIG;
import com.pacewear.tws.phoneside.wallet.common.Utils;
import com.pacewear.tws.phoneside.wallet.common.Utils.WalletCity;
import com.pacewear.tws.phoneside.wallet.env.EnvManager;
import com.pacewear.tws.phoneside.wallet.order.OrderManager;
import com.pacewear.tws.phoneside.wallet.pay.PayManager;
import com.pacewear.tws.phoneside.wallet.ui.widget.BottomBar;
import com.pacewear.tws.phoneside.wallet.ui.widget.BottomBar.OnBottomBarClickListener;
import com.pacewear.tws.phoneside.wallet.ui.widget.PayValueSelect;
import com.pacewear.tws.phoneside.wallet.ui.widget.PayValueSelect.OnSelectChangeListener;
import com.pacewear.tws.phoneside.wallet.ui.widget.SimpleCardListItem;
import com.qq.taf.jce.JceStruct;
import com.tencent.tws.assistant.app.ActionBar;
import com.tencent.tws.assistant.widget.CheckBox;
import com.tencent.tws.assistant.widget.Toast;
import com.tencent.tws.framework.global.GlobalObj;
import com.tencent.tws.pay.PayNFCConstants;

import java.util.ArrayList;

public class ActivateCardActivity extends TwsActivity {

    public static final String TAG = "ActivateCardActivity";

    private ICard mCard = null;

    // 支付配置
    private PayConfig mPayConfig = null;

    // 充值配置
    ArrayList<PayRechargeAmount> mPayRechargeAmount = null;

    private Context mContext = null;

    private BottomBar mBottomBar = null;

    private BaseAdapter mListAdapter = null;

    private static final int PAY_CHANNEL_WECHAT = 0;

    private static final int PAY_CHANNEL_QQ = PAY_CHANNEL_WECHAT + 1;

    private int mPayChannelSelected = PAY_CHANNEL_WECHAT;

    // 开卡费(单位元)
    private long mActivateFee = 3000;

    // 充值金额(单位元)
    private long mChargeValue = 3000;

    private SimpleCardListItem mSelectCityLayout = null;

    private final int CODE_REQ_SELECT_CITY = 1;

    private WalletCity mDefaultCity = new WalletCity();

    private WalletCity mSelectCity = new WalletCity();

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
            mCard = CardManager.getInstance().getCard(instanceId);// NFCHelper.getCard(type,
                                                                  // instanceId);
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
        QRomLog.d(TAG,
                "mPayConfig: " + JceStruct.toDisplaySimpleString(mPayConfig));

        // 开卡费 这命名简直是充值费！
        // TODO 优惠活动、确保开卡费合法
        mActivateFee = mPayConfig.getIChargeAmount();
        mPayRechargeAmount = mPayConfig.getVPayRechargeAmountList();

        // TODO
        if (mPayRechargeAmount == null || mPayRechargeAmount.size() < 3) {
            finish();
            return;
        }
        loadUserCityInfo();
        ActionBar actionBar = getTwsActionBar();
        actionBar.setBackgroundDrawable(new ColorDrawable(getResources()
                .getColor(R.color.wallet_action_bar_background)));
        actionBar.setTitle(getString(R.string.activate_card_title,
                mCard.getCardName()));

        Button actionLeftBt = (Button) actionBar.getCloseView(false);
        actionLeftBt.setText(getResources().getString(
                R.string.wallet_select_default_cancel));
        actionLeftBt.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                finish();
            }
        });

        setContentView(R.layout.wallet_activate_card);

        View tvProvision = findViewById(R.id.tv_wallet_confirm_provision);
        tvProvision.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
            }
        });

        mBottomBar = (BottomBar) findViewById(R.id.wallet_bottom_bar);
        mBottomBar.setMode(BottomBar.MODE_SINGLE_WITH_DESCRIPTION);
        QRomLog.d(TAG, String.format(getString(R.string.activate_card_fee),
                mActivateFee / 100));
        mBottomBar.setSingleSubDesText(String.format(
                getString(R.string.activate_card_fee), mActivateFee / 100));
        mBottomBar.setSingleButtonWithDesButtonEnable(false);
        mBottomBar.setOnBottomBarClickListener(new OnBottomBarClickListener() {

            @Override
            public boolean onSingleButtonWithDecClick() {
                // add by yuanqingqing
                if (!EnvManager.getInstance().isWatchConnected()) {
                    Toast.makeText(GlobalObj.g_appContext,
                            getString(R.string.wallet_disconnect_tips),
                            Toast.LENGTH_LONG).show();
                    return false;
                }
                if (!CardManager.getInstance().isReady()) {
                    Toast.makeText(GlobalObj.g_appContext,
                            getString(R.string.wallet_sync_err_watch),
                            Toast.LENGTH_LONG).show();
                    return false;
                }
                int payType = mPayChannelSelected == PAY_CHANNEL_QQ ? E_PAY_TYPE._E_PT_QQ_PAY
                        : E_PAY_TYPE._E_PT_WEIXIN_PAY;
                if (!PayManager.isPayChannelSupport(payType)) {
                    String qqTip = getString(R.string.wallet_login_download_qq);
                    String weixinTip = getString(R.string.wallet_login_download_mm);
                    Toast.makeText(GlobalObj.g_appContext,
                            (payType == PAY_CHANNEL_QQ) ? qqTip : weixinTip, Toast.LENGTH_LONG)
                            .show();
                    return false;
                }
                ShowLoadingActivity.launchLoading(
                        mContext,
                        mCard.getCardType(),
                        mCard.getAID(),
                        payType,
                        mActivateFee,
                        mActivateFee + mChargeValue,
                        ShowLoadingActivity.LOADING_TYPE_ACTIVATE_CARD, false);
                finish();
                return false;
            }

            @Override
            public boolean onSingleButtonClick() {
                return false;
            }

            @Override
            public boolean onCoupleRightButtonClick() {
                return false;
            }

            @Override
            public boolean onCoupleLeftButtonClick() {
                return false;
            }
        });

        CheckBox checked = (CheckBox) findViewById(R.id.activate_checked);
        checked.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton view, boolean enable) {
                mBottomBar.setSingleButtonWithDesButtonEnable(enable);
            }
        });
        checked.setChecked(true);
        mBottomBar.setSingleButtonWithDesButtonEnable(true);

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

        PayValueSelect mPayValueSelect = (PayValueSelect) findViewById(R.id.pay_value_select);
        mPayValueSelect.setPayRechargeAmount(mPayRechargeAmount);
        mPayValueSelect.setOnSelectChangeListener(new OnSelectChangeListener() {
            @Override
            public void onSelectChange(int which) {
                onSelected(which);
            }
        });
        mPayValueSelect.setSelect(OnSelectChangeListener.RIGHT_SELECTED);

        mSelectCityLayout = (SimpleCardListItem) findViewById(
                R.id.wallet_city_select);
        mSelectCityLayout.setIcon(R.drawable.wallet_ic_postion);
        mSelectCityLayout.setRightBitmap(R.drawable.arrow);
        mSelectCityLayout.setDescription(mDefaultCity.name);
        mSelectCityLayout.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                goSelectCity();
            }
        });

        ViewGroup layCitySelect = (ViewGroup) findViewById(R.id.wallet_city_lay);
        if (CONFIG.LINGNANTONG.mAID.equals(mCard.getAID())) {
            layCitySelect.setVisibility(View.VISIBLE);
        } else {
            layCitySelect.setVisibility(View.GONE);
        }
    }

    private void onSelected(int which) {
        switch (which) {
            case OnSelectChangeListener.LEFT_SELECTED:
                mChargeValue = mPayRechargeAmount.get(0).getITotalFee();
                break;
            case OnSelectChangeListener.MIDDLE_SELECTED:
                mChargeValue = mPayRechargeAmount.get(1).getITotalFee();
                break;
            case OnSelectChangeListener.RIGHT_SELECTED:
                mChargeValue = mPayRechargeAmount.get(2).getITotalFee();
                break;
        }

        mBottomBar.setSingleButtonWithDesMainDes("¥"
                + (mActivateFee + mChargeValue) / 100);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
            Intent data) {
        switch (requestCode) {
            case CODE_REQ_SELECT_CITY:
                if (resultCode == RESULT_OK) {
                    String name = data.getStringExtra(SelectCityActivity.SELECT_CITY_NAME);
                    String citycode = data.getStringExtra(SelectCityActivity.SELECT_CITY_CODE);
                    if (!TextUtils.isEmpty(name)) {
                        mSelectCityLayout.setDescription(name);
                        mSelectCity.name = name;
                    }
                    if (!TextUtils.isEmpty(citycode)) {
                        mCard.setExtra_Info("city_code", citycode);
                        mSelectCity.code = citycode;
                    }
                }
                break;
            default:
                break;
        }
    }

    private void goSelectCity() {
        Intent intent = new Intent();
        WalletCity city = Utils.getUserWalletCity();
        intent.setClass(ActivateCardActivity.this, SelectCityActivity.class);
        intent.putExtra(SelectCityActivity.CARD_NAME, mCard.getCardName());
        intent.putExtra(SelectCityActivity.SELECT_CITY_NAME, mSelectCity.name);
        intent.putExtra(SelectCityActivity.SELECT_CITY_CODE, mSelectCity.code);
        intent.putExtra(SelectCityActivity.DEFAULT_CITY_NAME, mDefaultCity.name);
        intent.putExtra(SelectCityActivity.DEFAULT_CITY_CODE, mDefaultCity.code);
        this.startActivityForResult(intent, CODE_REQ_SELECT_CITY);
    }

    private void loadUserCityInfo() {
        if (!CONFIG.LINGNANTONG.mAID.equalsIgnoreCase(mCard.getAID())) {
            return;
        }
        WalletCity tmpcity = Utils.getUserWalletCity();
        if (tmpcity == null) {
            return;
        }
        mDefaultCity.code = tmpcity.code;
        mDefaultCity.name = (TextUtils.isEmpty(tmpcity.name)) ? mCard.getCardName()
                : (mCard.getCardName() + "·" + tmpcity.name);
        mSelectCity.code = mDefaultCity.code;
        mSelectCity.name = mDefaultCity.name;
        mCard.setExtra_Info("city_code", tmpcity.code);
    }
}
