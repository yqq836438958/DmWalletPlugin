package com.pacewear.tws.phoneside.wallet.wxapi;

import qrom.component.log.QRomLog;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;

import com.tencent.mm.sdk.modelbase.BaseReq;
import com.tencent.mm.sdk.modelbase.BaseResp;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;
import com.tencent.tws.pay.PayNFCConstants;
import com.tencent.tws.phoneside.business.WeChatOAuthHelper;

public class WXPayEntryActivity extends Activity implements IWXAPIEventHandler{
	
	private static final String TAG = "NFC"+WXPayEntryActivity.class.getSimpleName();

    @Override
    public void onCreate(Bundle savedInstanceState) {
    	QRomLog.d(TAG, "onCreate");
    	
        super.onCreate(savedInstanceState);
        
		IWXAPI oApi =  WeChatOAuthHelper.getInstance().wxApi(); 
		assert( oApi != null );
		if( oApi != null ){
		    QRomLog.d(TAG, "WXEntryActivity oApi != null");
		    oApi.handleIntent(getIntent(), this);
		}
        finish();
    }

	@Override
	protected void onNewIntent(Intent intent) {
		QRomLog.d(TAG, "onNewIntent");
		
		super.onNewIntent(intent);
		
		setIntent(intent);
		
		IWXAPI oApi =  WeChatOAuthHelper.getInstance().wxApi(); 
		assert( oApi != null );
		if( oApi != null ){
		    QRomLog.d(TAG, "WXEntryActivity oApi != null");
		    oApi.handleIntent(getIntent(), this);
		}
		
		finish();
	}

	@Override
	public void onReq(BaseReq req) {
	}

	@Override
	public void onResp(BaseResp resp) {
		QRomLog.d(TAG, "onResp");
		if (resp == null) {
			QRomLog.e(TAG, "onResp|invalid arg");
			return;
		}
		Bundle rspBundle = new Bundle();
		resp.toBundle(rspBundle);
		QRomLog.d(TAG, "onResp|resp: " + rspBundle.toString());
		
		Intent intent = new Intent(PayNFCConstants.ACTION_WXPAY_RESULT_NOTIFY);
		intent.setPackage(getPackageName());
		intent.putExtras(rspBundle);
		
		LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
	}
}