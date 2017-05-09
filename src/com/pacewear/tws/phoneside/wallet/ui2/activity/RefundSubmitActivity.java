
package com.pacewear.tws.phoneside.wallet.ui2.activity;

import android.app.TwsActivity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.pacewear.tws.phoneside.wallet.R;
import com.pacewear.tws.phoneside.wallet.WalletApp;
import com.pacewear.tws.phoneside.wallet.common.PhoneFormatCheckUtils;

//plan 2.0
public class RefundSubmitActivity extends TwsWalletActivity {
    @Override
    protected void onCreate(Bundle arg0) {
        // wallet2_fragment_refund_result
        super.onCreate(arg0);
        setContentView(R.layout.wallet2_actvitiy_refund_submit);
        setActionBar(R.string.wallet_title_refunding, new LeftCancleRightHelpStagy());
        final EditText nameEdit = (EditText) findViewById(R.id.name);
        final EditText phoneEdit = (EditText) findViewById(R.id.phone);
        final Button confirm = (Button) findViewById(R.id.submit);
        phoneEdit.addTextChangedListener(new TextWatcher() {

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
                if (nameEdit.getText().length() > 0 && s.length() == 11) {
                    confirm.setEnabled(true);
                } else {
                    confirm.setEnabled(false);
                }

            }
        });
        confirm.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                String phone = phoneEdit.getText().toString();
                if (!PhoneFormatCheckUtils.isPhoneLegal(phone)) {
                    Toast.makeText(WalletApp.getHostAppContext(),
                            getString(R.string.wallet_phone_error),
                            Toast.LENGTH_LONG).show();
                    return;
                }
                doRefundReq();
            }
        });
        confirm.setEnabled(false);
    }

    private void doRefundReq() {
        // TODO
        goRefundResultPage();
    }

    private void goRefundResultPage() {
        Intent it = new Intent();
        it.setClass(this, RefundResultActivity.class);
        startActivity(it);
        finish();
    }
}
