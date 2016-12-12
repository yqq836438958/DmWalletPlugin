
package com.pacewear.tws.phoneside.wallet.ui;

import TRom.BusCardInfo;
import TRom.CustomServiceRsp;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.app.TwsActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.pacewear.tws.phoneside.wallet.R;
import com.pacewear.tws.phoneside.wallet.card.CardManager;
import com.pacewear.tws.phoneside.wallet.card.ICard;
import com.pacewear.tws.phoneside.wallet.card.ICard.CARD_TYPE;
import com.pacewear.tws.phoneside.wallet.card.ITrafficCard;
import com.pacewear.tws.phoneside.wallet.common.UIHelper;
import com.pacewear.tws.phoneside.wallet.common.Utils;
import com.pacewear.tws.phoneside.wallet.tosservice.CustomService;
import com.pacewear.tws.phoneside.wallet.tosservice.IResponseObserver;
import com.qq.taf.jce.JceStruct;
import com.tencent.tws.assistant.app.ActionBar;
import com.tencent.tws.assistant.widget.ToggleButton;
import com.tencent.tws.assistant.widget.TwsButton;

import qrom.component.log.QRomLog;

import java.util.ArrayList;

public class CustomerSupportActivity extends TwsActivity implements OnClickListener {

    private ActionBar mTwsActionBar;

    private ImageView mCardImageView;

    private RelativeLayout mInPutLayout;

    private TextView mSummaryTextView;

    private String mUserName;

    private String mUserPhone;

    private TwsButton mConfirmButton;

    private EditText mNameEditText;

    private EditText mPhoneEditText;

    private String AID = null;

    private static final String TAG = CustomService.class.getSimpleName();

    private long mUniqueReq;

    private float mCardTranslationDistance = 0f;

    private static int mAninmationTime = 300;

    private Runnable mClickRunnable = new Runnable() {

        @Override
        public void run() {
            // TODO Auto-generated method stub
            CustomService customService = new CustomService();
            mUniqueReq = customService.getUniqueSeq();
            customService.setUserPersonalInfo(mUserName, mUserPhone, getCardInfoList());
            customService.invoke(new IResponseObserver() {

                @Override
                public void onResponseSucceed(long uniqueSeq, int operType, JceStruct response) {
                    if (uniqueSeq == mUniqueReq) {
                        CustomServiceRsp rsp = (CustomServiceRsp) response;
                        QRomLog.d(TAG, "onResponseSucceed:" + rsp.getIRet());
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                // TODO Auto-generated method stub
                                Toast.makeText(getApplicationContext(),
                                        R.string.wallet_support_submit_personal_info_complete,
                                        Toast.LENGTH_LONG)
                                        .show();
                            }
                        });
                        finish();

                    }
                }

                @Override
                public void onResponseFailed(long uniqueSeq, int operType, int errorCode,
                        String description) {
                    QRomLog.d(TAG, "onResponseFailed:");
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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE |
        // WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        Intent intent = getIntent();
        if (intent == null) {
            finish();
        }

        initActionBar();
        setContentView(R.layout.wallet_customer_support_activity);
        initViews();
        AID = intent.getStringExtra("AID");
        int resid = intent.getIntExtra("tittle_resid", R.string.wallet_support_summary);
        mCardTranslationDistance = getResources().getDimension(R.dimen.card_translation_y_distance);
        mSummaryTextView.setText(resid);
        if (resid == R.string.wallet_support_personal_info_has_submit) {
            mConfirmButton.setVisibility(View.GONE);
        }
        ICard card = CardManager.getInstance().getCard(AID);
        mCardImageView.setBackgroundResource(card.getCardBg());

    }

    private void initViews() {
        mCardImageView = (ImageView) findViewById(R.id.wallet_card_imageview_id);
        mConfirmButton = (TwsButton) findViewById(R.id.wallet_start_filling_button_id);
        UIHelper.setTwsButton(mConfirmButton, R.string.wallet_support_start_filling, 14);
        mConfirmButton.setOnClickListener(this);
        mSummaryTextView = (TextView) findViewById(R.id.wallet_summary_tv_id);
        mInPutLayout = (RelativeLayout) findViewById(R.id.wallet_input_layout_id);
        mPhoneEditText = (EditText) findViewById(R.id.phone_edt_id);
        mNameEditText = (EditText) findViewById(R.id.name_edt_id);
    }

    private void initActionBar() {
        mTwsActionBar = getTwsActionBar();
        mTwsActionBar.setTitle(getString(R.string.wallet_support_actionbar_title));

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        Utils.getWorkerHandler().removeCallbacks(mClickRunnable);
        super.onDestroy();
    }

    @Override
    public void finish() {
        super.finish();
        // overridePendingTransition(0, R.anim.wallet_push_down);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.wallet_start_filling_button_id:
                showInputView();
                break;

            default:
                break;
        }

    }

    private void showInputView() {
        mSummaryTextView.setVisibility(View.GONE);
        mConfirmButton.setVisibility(View.GONE);
        mInPutLayout.setVisibility(View.VISIBLE);
        ValueAnimator animator = ValueAnimator.ofFloat(0f, -mCardTranslationDistance);
        animator.setDuration(mAninmationTime);
        animator.addUpdateListener(new AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                Object value = animation.getAnimatedValue();
                if (value == null) {
                    QRomLog.w(TAG, "animator onAnimationUpdate animation.getAnimatedValue is NULL");
                    return;
                }

                float translationY = 0;
                if (value instanceof Float) {
                    translationY = ((Float) value).floatValue();
                }
                mCardImageView.setTranslationY(translationY);
            }
        });
        animator.start();
        AnimatorSet set = new AnimatorSet();
        ValueAnimator alphaAnimator = ValueAnimator.ofFloat(0.0f, 1.0f);
        alphaAnimator.setDuration(mAninmationTime);
        alphaAnimator.addUpdateListener(new AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                Object value = animation.getAnimatedValue();
                if (value == null) {
                    QRomLog.w(TAG,
                            "alphaAnimator onAnimationUpdate animation.getAnimatedValue is NULL");
                    return;
                }

                float alpha = 0;
                if (value instanceof Float) {
                    alpha = ((Float) value).floatValue();
                }
                mInPutLayout.setAlpha(alpha);
            }
        });
        set.play(alphaAnimator).after(animator);
        set.addListener(new AnimatorListener() {

            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                showSubmitButton();
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                // TODO Auto-generated method stub

            }
        });
        set.start();
    }

    private void showSubmitButton() {

        ToggleButton btn = (ToggleButton) mTwsActionBar.getMultiChoiceView(false);
        String submitString = getString(R.string.wallet_support_submit_string);
        btn.setText(submitString);
        btn.setTextOn(submitString);
        btn.setTextOff(submitString);
        btn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                mUserName = mNameEditText.getText().toString();
                mUserPhone = mPhoneEditText.getText().toString();
                QRomLog.d(TAG, "name:" + mUserName + "   phone:" + mUserPhone);
                if ((mUserName != null && mUserName.equals(""))
                        || (mUserPhone != null && mUserPhone.equals(""))) {
                    Toast.makeText(getApplicationContext(),
                            R.string.wallet_support_improve_personal_info,
                            Toast.LENGTH_LONG).show();
                    return;
                }
                Utils.getWorkerHandler().removeCallbacks(mClickRunnable);
                Utils.getWorkerHandler().post(mClickRunnable);

            }
        });

    }

    private ArrayList<BusCardInfo> getCardInfoList() {

        if (CardManager.getInstance().isReady()) {
            ArrayList<BusCardInfo> busCardBaseInfos = new ArrayList<BusCardInfo>();
            ArrayList<ICard> trafficCards = CardManager.getInstance()
                    .getCard(CARD_TYPE.TRAFFIC_CARD);
            for (int i = 0; i < trafficCards.size(); i++) {
                BusCardInfo busCardInfo = new BusCardInfo();
                ITrafficCard card = ((ITrafficCard) trafficCards.get(i));
                busCardInfo.setStBusCardBaseInfo(card.getBusCardBaseInfo());
                busCardInfo.setSCardBalance(card.getBalance());
                busCardBaseInfos.add(busCardInfo);
                QRomLog.d(TAG, i + "---> sCardNum:" + card.getBusCardBaseInfo().getSCardNum()
                        + "  sIssuerName:"
                        + card.getBusCardBaseInfo().getSIssuerName() + "  sInstanceAId:"
                        + card.getBusCardBaseInfo().getSInstanceAId() + "  Balance:"
                        + card.getBalance());

            }
            return busCardBaseInfos;

        }

        return null;

    }

}
