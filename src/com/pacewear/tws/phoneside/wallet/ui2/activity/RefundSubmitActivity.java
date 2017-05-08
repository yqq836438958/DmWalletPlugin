
package com.pacewear.tws.phoneside.wallet.ui2.activity;

import android.app.TwsActivity;
import android.content.Intent;
import android.os.Bundle;

import com.pacewear.tws.phoneside.wallet.R;

//plan 2.0
public class RefundSubmitActivity extends TwsActivity {
    @Override
    protected void onCreate(Bundle arg0) {
        // wallet2_fragment_refund_result
        super.onCreate(arg0);
        setContentView(R.layout.wallet2_actvitiy_refund_submit);
    }

    private void goRefundResultPage() {
        Intent it = new Intent();
        it.setClass(this, RefundResultActivity.class);
        startActivity(it);
    }
}
