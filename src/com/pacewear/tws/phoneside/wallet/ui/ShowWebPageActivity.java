
package com.pacewear.tws.phoneside.wallet.ui;

import android.app.TwsActivity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;

import com.tencent.tws.assistant.app.ActionBar;
import com.tencent.tws.assistant.widget.ToggleButton;
import com.tencent.tws.gdevicemanager.R;
import com.tencent.tws.pay.PayNFCConstants;
import com.tencent.tws.phoneside.utils.DensityUtil;

import qrom.component.log.QRomLog;

public class ShowWebPageActivity extends TwsActivity {

    public static final String TAG = "ShowWebPageActivity";

    private Context mContext = null;

    private WebView mWebView = null;

    private String mTitle = null;

    private String mURL = null;

    private static final String TITLE = "SHOW_TITLE";

    private static final String URL = "SHOW_URL";

    private static final String LEFT_BTN = "LEFT_BTN";

    private static final String RIGHT_BTN = "RIGHT_BTN";

    public static final String TARGET_CLASS = "TARGET_CLASS";

    public static final String TARGET_AID = "TARGET_AID";

    public static void putTitle(Intent intent, String title) {
        intent.putExtra(TITLE, title);
    }

    public static void putURL(Intent intent, String url) {
        intent.putExtra(URL, url);
    }

    public static void putActionBarText(Intent intent, String leftTxt, String rightTxt) {
        intent.putExtra(LEFT_BTN, leftTxt);
        intent.putExtra(RIGHT_BTN, rightTxt);
    }

    private String getTitle(Intent intent) {
        return intent.getStringExtra(TITLE);
    }

    private String getURL(Intent intent) {
        return intent.getStringExtra(URL);
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, R.anim.wallet_push_down);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        overridePendingTransition(R.anim.wallet_push_up, 0);

        super.onCreate(savedInstanceState);

        setContentView(R.layout.wallet_show_web_page);

        mContext = this;

        Intent intent = getIntent();
        if (intent != null) {
            mTitle = getTitle(intent);
            mURL = getURL(intent);
        }
        String leftBtnTxt = intent.getStringExtra(LEFT_BTN);
        String rightBtnTxt = intent.getStringExtra(RIGHT_BTN);
        final String targetClass = intent.getStringExtra(TARGET_CLASS);
        final String aid = intent.getStringExtra(TARGET_AID);
        QRomLog.d(TAG, TITLE + ": " + mTitle);
        QRomLog.d(TAG, URL + ": " + mURL);

        ActionBar actionBar = getTwsActionBar();
        actionBar.setBackgroundDrawable(
                new ColorDrawable(getResources().getColor(R.color.wallet_action_bar_background)));
        if (mTitle != null) {
            actionBar.setTitle(mTitle);
        }
        if (!TextUtils.isEmpty(rightBtnTxt)) {
            ToggleButton btn = (ToggleButton) actionBar.getMultiChoiceView(false);
            btn.setPadding(0, 0, DensityUtil.dip2px(this, 20), 0);
            btn.setText(rightBtnTxt);
            btn.setTextOn(rightBtnTxt);
            btn.setTextOff(rightBtnTxt);
            btn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    goTargetActivity(targetClass, aid);
                }
            });
        }

        Button actionLeftBt = (Button) actionBar.getCloseView(false);
        if (!TextUtils.isEmpty(leftBtnTxt)) {
            actionLeftBt.setText(leftBtnTxt);
        } else {
            actionLeftBt.setText(getResources().getString(
                    R.string.wallet_operation_result_close));
        }
        actionLeftBt.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                finish();
            }
        });

        mWebView = (WebView) findViewById(R.id.webview);
        if (mURL != null) {
            WebSettings webSettings = mWebView.getSettings();
            if (webSettings != null) {
                webSettings.setJavaScriptEnabled(true);
                webSettings.setSupportZoom(false);
            }
            mWebView.clearCache(true);
            mWebView.clearHistory();
            mWebView.setBackgroundColor(
                    getResources().getColor(R.color.wallet_action_bar_background));
            mWebView.setWebViewClient(new WebViewClient() {
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    view.loadUrl(url);
                    return true;
                }
            });
            mWebView.loadUrl(mURL);
        }
    }

    private void goTargetActivity(String targetActivity, String aid) {
        if (TextUtils.isEmpty(targetActivity) || TextUtils.isEmpty(aid)) {
            return;
        }
        Intent intent = new Intent();
        intent.setClassName(ShowWebPageActivity.this, targetActivity);
        intent.putExtra(PayNFCConstants.ExtraKeyName.EXTRA_STR_INSTANCE_ID,
                aid);
        this.startActivity(intent);
        finish();
    }
}
