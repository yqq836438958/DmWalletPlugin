
package com.pacewear.tws.phoneside.wallet.ui2.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.pacewear.tws.phoneside.wallet.R;
import com.pacewear.tws.phoneside.wallet.WalletApp;
import com.pacewear.tws.phoneside.wallet.bean.OrderBean;
import com.pacewear.tws.phoneside.wallet.card.CardManager;
import com.pacewear.tws.phoneside.wallet.card.ICard;
import com.pacewear.tws.phoneside.wallet.card.ICardInner.CONFIG;
import com.pacewear.tws.phoneside.wallet.common.FontsOverride;
import com.pacewear.tws.phoneside.wallet.common.Utils;
import com.pacewear.tws.phoneside.wallet.common.Utils.WalletCity;
import com.pacewear.tws.phoneside.wallet.order.OrderManager;
import com.pacewear.tws.phoneside.wallet.ui2.widget.BaseCardView;
import com.pacewear.tws.phoneside.wallet.ui2.widget.PayValueSelect;
import com.pacewear.tws.phoneside.wallet.ui2.widget.PayValueSelect.OnSelectChangeListener;
import com.pacewear.tws.phoneside.wallet.ui2.widget.SimpleCardListItem;
import com.pacewear.tws.phoneside.wallet.ui2.widget.TrafficCardView;
import com.qq.taf.jce.JceStruct;
import com.tencent.tws.assistant.widget.Toast;
import com.tencent.tws.pay.PayNFCConstants;

import java.util.ArrayList;

import TRom.E_PAY_SCENE;
import TRom.PayConfig;
import TRom.PayRechargeAmount;
import qrom.component.log.QRomLog;

public class CardIssuePrepareActivity extends TwsWalletActivity {

    public static final String TAG = "ActivateCardActivity";

    private ICard mCard = null;

    // 支付配置
    private PayConfig mPayConfig = null;

    // 充值配置
    ArrayList<PayRechargeAmount> mPayRechargeAmount = null;

    private Context mContext = null;

    // 开卡费(单位元)
    private long mActivateFee = 0;

    // 充值金额(单位元)
    private long mChargeValue = 0;

    private long mActivityAmount = 0L;
    private SimpleCardListItem mSelectCityLayout = null;

    private final int CODE_REQ_SELECT_CITY = 1;
    private final int CODE_REQ_CHOOSE_PAY = 2;
    private WalletCity mDefaultCity = new WalletCity();

    private WalletCity mSelectCity = new WalletCity();

    private TextView mTotalFeeTv = null;
    private TextView mIssueFeeTv = null;

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

        mActivityAmount = (mPayConfig.getIActivityFlag() == 1) ? mPayConfig.getIActivityAmount()
                : 0;
        // TODO
        if (mPayRechargeAmount == null || mPayRechargeAmount.size() < 3) {
            finish();
            return;
        }
        loadUserCityInfo();
        final boolean isNewLntSupport = isLingNanTongNewSupport();
        setActionBar(getString(R.string.activate_card_title,
                mCard.getCardName()), new LeftCancleRightHelpStagy());

        setContentView(R.layout.wallet2_activity_cardissue);
        mTotalFeeTv = (TextView) findViewById(R.id.tv_totalfee);
        mIssueFeeTv = (TextView) findViewById(R.id.tv_issuefee);
        mTotalFeeTv.setTypeface(FontsOverride.getDigitFont(mContext));
        mIssueFeeTv.setText(String.format(
                getString(R.string.activate_card_fee), mActivateFee / 100));
        TrafficCardView cardView = (TrafficCardView) findViewById(R.id.wallet_card_detail_card);
        cardView.attachCard(mCard, BaseCardView.SENCE_LITE);
        PayValueSelect payValueSelect = (PayValueSelect) findViewById(R.id.pay_value_select);
        if (!isNewLntSupport) {
            payValueSelect.setPayRechargeAmount(mPayRechargeAmount);
            payValueSelect.setOnSelectChangeListener(new OnSelectChangeListener() {
                @Override
                public void onSelectChange(int which) {
                    onSelected(which);
                }
            });
            payValueSelect.setSelect(OnSelectChangeListener.RIGHT_SELECTED);
        } else {
            onSelected(0);
            findViewById(R.id.denomination_notice).setVisibility(View.GONE);
            payValueSelect.setVisibility(View.GONE);
        }

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

        findViewById(R.id.wallet_city_lay)
                .setVisibility(isNewLntSupport ? View.VISIBLE : View.GONE);
        Button confirm = (Button) findViewById(R.id.confirm);
        confirm.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                goPayChoosePage(mActivateFee + mChargeValue - mActivityAmount);
            }
        });
    }

    private boolean isLingNanTongNewSupport() {
        return CONFIG.LINGNANTONG.mAID.equalsIgnoreCase(mCard.getAID());
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
            default:
                mChargeValue = 0;
                break;
        }

        mTotalFeeTv.setText("¥"
                + Utils.getDisplayBalance(mActivateFee + mChargeValue - mActivityAmount));
    }

    private void goPayChoosePage(long chargeVal) {
        Intent intent = new Intent(this, PayChooseActivity.class);
        intent.putExtra(PayChooseActivity.PAY_AMOUNT, chargeVal);
        intent.putExtra(PayChooseActivity.PAY_DESC, mCard.getCardName());
        startActivityForResult(intent, CODE_REQ_CHOOSE_PAY);
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
                        Utils.saveUserCitycode(citycode);
                        mSelectCity.code = citycode;
                    }
                }
                break;
            case CODE_REQ_CHOOSE_PAY:
                if (resultCode == RESULT_OK) {
                    goIssuePageInternal(data.getIntExtra(PayChooseActivity.PAY_TYPE, 0),
                            data.getLongExtra(PayChooseActivity.PAY_AMOUNT, 0L));
                } else {
                    Toast.makeText(WalletApp.getHostAppContext(), "cancel pay", Toast.LENGTH_LONG)
                            .show();
                }
            default:
                break;
        }
    }

    private void goIssuePageInternal(int payType, long totalFee) {
        Intent intent = new Intent(this, BusinessLoadingActivity.class);
        int scene = isLingNanTongNewSupport() ? E_PAY_SCENE._EPS_OPEN_CARD_ONLY
                : E_PAY_SCENE._EPS_OPEN_CARD;
        OrderBean bean = OrderBean.genNewInstance(mCard.getAID(), payType,
                scene, mActivateFee,
                totalFee, false);
        intent.putExtra(BusinessLoadingActivity.KEY_ORDER_BEAN, bean);
        startActivity(intent);
        finish();
    }

    private void goSelectCity() {
        Intent intent = new Intent();
        WalletCity city = Utils.getUserWalletCity();
        intent.setClass(this, SelectCityActivity.class);
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
        Utils.saveUserCitycode(tmpcity.code);
    }
}
