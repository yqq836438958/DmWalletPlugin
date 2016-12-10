
package com.pacewear.tws.phoneside.wallet.ui;

import qrom.component.log.QRomLog;

import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mobileqq.openpay.api.IOpenApi;
import com.tencent.mobileqq.openpay.api.IOpenApiListener;
import com.tencent.mobileqq.openpay.api.OpenApiFactory;
import com.tencent.mobileqq.openpay.data.base.BaseResponse;
import com.tencent.mobileqq.openpay.data.pay.PayResponse;
import com.tencent.tws.pay.PayNFCConstants;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;

public class QQPayEntryActivity extends Activity implements IOpenApiListener {
    private static final String TAG = "QQPayEntryActivity";

    IOpenApi openApi;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        QRomLog.d(TAG, "onCreate");

        super.onCreate(savedInstanceState);

        openApi = OpenApiFactory.getInstance(this, PayNFCConstants.APP_ID_FOR_QQPAY);
        openApi.handleIntent(getIntent(), this);

        finish();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        QRomLog.d(TAG, "onNewIntent");

        super.onNewIntent(intent);
        setIntent(intent);
        openApi.handleIntent(intent, this);
        finish();
    }

    @Override
    public void onOpenResponse(BaseResponse response) {
        QRomLog.d(TAG, "onOpenResponse");
        if (response == null) {
            QRomLog.e(TAG, "onOpenResponse|response null");
            return;
        }

        if (!(response instanceof PayResponse)) {
            QRomLog.e(TAG, "onOpenResponse|not pay response");
            return;
        }

        PayResponse payResponse = (PayResponse) response;
        Bundle rspBundle = new Bundle();
        payResponse.toBundle(rspBundle);
        QRomLog.d(TAG, "onOpenResponse|resp: " + rspBundle.toString());

        Intent intent = new Intent(PayNFCConstants.ACTION_QQPAY_RESULT_NOTIFY);
        intent.setPackage(getPackageName());
        intent.putExtras(rspBundle);

        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
}
