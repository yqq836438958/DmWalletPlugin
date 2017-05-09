
package com.pacewear.tws.phoneside.wallet.ui2.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.pacewear.tws.phoneside.wallet.R;

public class HelpActivity extends TwsWalletActivity {
    public static final String KEY_HELP = "key_help";

    @Override
    public void finish() {
        super.finish();
        // overridePendingTransition(0, R.anim.wallet_push_down);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        // overridePendingTransition(R.anim.wallet_push_up, 0);

        super.onCreate(savedInstanceState);

        setContentView(R.layout.wallet_show_web_page);
        setActionBar(R.string.wallet_help_qa, new LeftCloseTextStagy());
        WebView webView = (WebView) findViewById(R.id.webview);
        String url = getIntent().getStringExtra(KEY_HELP);
        if (!TextUtils.isEmpty(url)) {
            WebSettings webSettings = webView.getSettings();
            if (webSettings != null) {
                webSettings.setJavaScriptEnabled(true);
                webSettings.setSupportZoom(false);
            }
            webView.clearCache(true);
            webView.clearHistory();
            webView.setBackgroundColor(
                    getResources().getColor(R.color.wallet_action_bar_background));
            webView.setWebViewClient(new WebViewClient() {
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    view.loadUrl(url);
                    return true;
                }
            });
            webView.loadUrl(url);
        }
    }
}
