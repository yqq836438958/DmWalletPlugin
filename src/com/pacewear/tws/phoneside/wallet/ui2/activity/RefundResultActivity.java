
package com.pacewear.tws.phoneside.wallet.ui2.activity;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

import com.pacewear.tws.phoneside.wallet.R;

//plan 2.0
public class RefundResultActivity extends TwsWalletActivity {
    public static final String KEY_REFUND_RESULT = "key_refund_result";

    @Override
    protected void onCreate(Bundle arg0) {
        // wallet2_fragment_refund_result
        super.onCreate(arg0);
        setContentView(R.layout.wallet2_actvitiy_refund_result);
        hideActionBar();
        int result = getIntent().getIntExtra(KEY_REFUND_RESULT, 0);
        findViewById(R.id.wallet_operation_result_close)
                .setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        finish();
                    }
                });
    }
}
