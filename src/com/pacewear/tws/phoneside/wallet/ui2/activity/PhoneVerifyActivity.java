
package com.pacewear.tws.phoneside.wallet.ui2.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.pacewear.tws.phoneside.wallet.R;
import com.pacewear.tws.phoneside.wallet.WalletApp;
import com.pacewear.tws.phoneside.wallet.common.ClickFilter;
import com.pacewear.tws.phoneside.wallet.common.PhoneFormatCheckUtils;
import com.pacewear.tws.phoneside.wallet.common.Utils;
import com.pacewear.tws.phoneside.wallet.ui2.widget.TimerButton;
import com.tencent.tws.phoneside.phoneverify.SmsModel;
import com.tencent.tws.phoneside.phoneverify.SmsModel.OnSmsCallback;

public class PhoneVerifyActivity extends TwsWalletActivity implements View.OnClickListener {
    private EditText mPhoneEdit = null;
    private EditText mCodeEdit = null;
    private String mPhoneNum = null;
    private String mVerifyCode = null;
    private TimerButton mVerifyBtn = null;
    private Button mConfirm = null;
    private static final int STEP_GETVERIFY = 0;
    private static final int STEP_CONFIRM = 1;
    private int mStep = STEP_GETVERIFY;
    public static final String PHONENUM = "phonenum";
    public static final String HINT_TEXT = "hint_text";
    private SmsModel mSmsModel = null;
    private OnSmsCallback mOnSmsCallback = new OnSmsCallback() {

        @Override
        public void OnResult(int ret, String result) {
            if (ret == 0 || ret == 1) {
                showSuccessResult(ret == 1 ? true : false);
            } else {
                showErrorTip();
            }

        }
    };
    private Runnable mVerifyRunnable = new Runnable() {

        @Override
        public void run() {
            if (mStep == STEP_GETVERIFY) {
                mSmsModel.getSecurityCode(mPhoneNum, mOnSmsCallback);
            } else {
                mSmsModel.sendSecurityCode(mPhoneNum, mVerifyCode, mOnSmsCallback);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wallet2_phone_verify);
        mSmsModel = SmsModel.get();
        Intent intent = getIntent();
        String hintText = intent.getStringExtra(HINT_TEXT);
        initViews(savedInstanceState, hintText);
    }

    private void initViews(Bundle savedInstanceState, String hintText) {
        mVerifyBtn = (TimerButton) findViewById(R.id.wallet_verify_btn);
        mPhoneEdit = (EditText) findViewById(R.id.wallet_phonenum_val);
        if (hintText != null && !hintText.isEmpty()) {
            mPhoneEdit.setHintTextColor(
                    getResources().getColor(R.color.wallet_detail_bt_text_disabled));
            mPhoneEdit.setHint(hintText);
        }
        mCodeEdit = (EditText) findViewById(R.id.wallet_verifycode_val);
        mConfirm = (Button) findViewById(R.id.phone_submit);
        setActionBar(R.string.wallet_infoverify, new LeftCancleStagy());

        mVerifyBtn.onCreate(savedInstanceState);
        mPhoneEdit.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // TODO Auto-generated method stub
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // TODO Auto-generated method stub
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (mPhoneEdit.getText().length() > 10) {
                    mVerifyBtn.setButtonEnable(true);
                } else {
                    mVerifyBtn.setButtonEnable(false);
                }
            }
        });
        mCodeEdit.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {

            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
            }

            @Override
            public void afterTextChanged(Editable arg0) {
                if (mPhoneEdit.getText().length() > 10 && arg0.length() > 3 && arg0.length() < 9) {
                    mConfirm.setEnabled(true);
                } else {
                    mConfirm.setEnabled(false);
                }
            }
        });
        mVerifyBtn.setOnClickListener(this);
        mConfirm.setOnClickListener(this);
        mConfirm.setEnabled(false);
    }

    @Override
    public void onClick(View arg0) {
        if (ClickFilter.isMultiClick()) {
            return;
        }
        if (arg0.equals(mConfirm)) {
            onHandleClick(0);
        } else if (arg0.equals(mVerifyBtn)) {
            onHandleClick(1);
        }
    }

    private void onHandleClick(int type) {
        if (type == 1) {
            mStep = STEP_GETVERIFY;
        } else {
            mStep = STEP_CONFIRM;
        }
        String phone = mPhoneEdit.getText().toString();
        if (!PhoneFormatCheckUtils.isPhoneLegal(phone)) {
            Toast.makeText(WalletApp.getHostAppContext(), getString(R.string.wallet_phone_error),
                    Toast.LENGTH_LONG).show();
            return;
        }
        mPhoneNum = phone;
        mVerifyCode = mCodeEdit.getText().toString();
        Utils.getWorkerHandler().removeCallbacks(mVerifyRunnable);
        Utils.getWorkerHandler().post(mVerifyRunnable);
    }

    private void showErrorTip() {
        this.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                if (mStep == STEP_GETVERIFY) {
                    Toast.makeText(WalletApp.getHostAppContext(),
                            getString(R.string.wallet_get_verify_fail),
                            Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(WalletApp.getHostAppContext(),
                            getString(R.string.wallet_verify_confirm_fail),
                            Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void showSuccessResult(final boolean directFinishPage) {
        this.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                if (mStep == STEP_CONFIRM || directFinishPage) {
                    Intent intent = new Intent();
                    intent.putExtra(PHONENUM, mPhoneNum);
                    PhoneVerifyActivity.this.setResult(Activity.RESULT_OK, intent);
                    finish();
                } else {
                    mVerifyBtn.onPerformClick();
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        mVerifyBtn.onDestroy();
        super.onDestroy();
    }

    // private void setButton
}
