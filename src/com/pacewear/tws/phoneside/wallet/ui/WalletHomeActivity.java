
package com.pacewear.tws.phoneside.wallet.ui;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.TwsActivity;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.pacewear.httpserver.ServerHandler;
import com.pacewear.tws.phoneside.wallet.R;
import com.pacewear.tws.phoneside.wallet.card.CardManager;
import com.pacewear.tws.phoneside.wallet.card.ICard;
import com.pacewear.tws.phoneside.wallet.card.ICard.CARD_TYPE;
import com.pacewear.tws.phoneside.wallet.card.ICard.INSTALL_STATUS;
import com.pacewear.tws.phoneside.wallet.card.ICardInner.CONFIG;
import com.pacewear.tws.phoneside.wallet.common.Constants;
import com.pacewear.tws.phoneside.wallet.common.Utils;
import com.pacewear.tws.phoneside.wallet.env.EnvManager;
import com.pacewear.tws.phoneside.wallet.order.OrderManager;
import com.pacewear.tws.phoneside.wallet.tsm.TsmTestActivity;
import com.pacewear.tws.phoneside.wallet.ui.fragments.CardsFragment;
import com.pacewear.tws.phoneside.wallet.ui.handler.WalletHandlerManager;
import com.pacewear.tws.phoneside.wallet.ui.handler.WalletBaseHandler.ACTVITY_SCENE;
import com.pacewear.tws.phoneside.wallet.ui.handler.WalletBaseHandler.MODULE_CALLBACK;
import com.pacewear.tws.phoneside.wallet.ui.handler.WalletBaseHandler.OnWalletUICallback;
import com.tencent.tws.assistant.app.ActionBar;
import com.tencent.tws.phoneside.utils.BranchUtil;

import qrom.component.log.QRomLog;

public class WalletHomeActivity extends TwsActivity implements OnWalletUICallback {
    private View mLoading = null;
    private View mErrorView = null;
    private View mWaiting = null;
    private TextView mErrorTextView = null;
    private boolean mIs3rdIssueCard = false;
    private CardsFragment mTrafficCardsFragment = null;
    long[] mTitleViewHits = new long[5];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wallet_home);
        initViews();
        onPostCreate();
        loadIntentIfNeed();
    }

    @Override
    protected void onResume() {
        super.onResume();
        WalletHandlerManager.getInstance().requestFocus(ACTVITY_SCENE.SCENE_SYNCALL);
        mTrafficCardsFragment.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mTrafficCardsFragment.onPause();
    }

    @Override
    protected void onDestroy() {
        WalletHandlerManager.getInstance().unregister(ACTVITY_SCENE.SCENE_SYNCALL);
        super.onDestroy();
    }

    private void initViews() {
        mWaiting = findViewById(R.id.wallet_main_wait);
        if (!isModuleAvailable()) {
            mWaiting.setVisibility(View.VISIBLE);
        }
        mLoading = findViewById(R.id.wallet_main_loading_ly);
        mErrorView = findViewById(R.id.wallet_main_error);
        mErrorTextView = (TextView) findViewById(R.id.wallet_main_error_tv);
        mTrafficCardsFragment = new CardsFragment(CARD_TYPE.TRAFFIC_CARD);
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.wallet_content, mTrafficCardsFragment);
        transaction.commit();
        if (!CardManager.getInstance().isAvaliable()) {
            showErrorPage(true, 0);
        }
        ActionBar actionBar = getTwsActionBar();
        actionBar.setBackgroundDrawable(new ColorDrawable(getResources()
                .getColor(R.color.wallet_action_bar_background)));
        actionBar.setTitle(R.string.nfc_wallet);
        checkTestFunction(actionBar);
    }

    private void onPostCreate() {
        EnvManager.getInstance().forceSyncCPLC(false);
        CardManager.getInstance().forceUpdate(true);
        OrderManager.getInstance().forceSyncTrafficConfig(true);
        OrderManager.getInstance().forceSyncOrder(true);
        WalletHandlerManager.getInstance().register(null, ACTVITY_SCENE.SCENE_SYNCALL, this);
    }

    @Override
    public void onUIUpdate(final MODULE_CALLBACK module, final int ret, final boolean forUpdateUI) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!isModuleAvailable()) {
                    mWaiting.setVisibility(View.VISIBLE);
                    return;
                }
                mWaiting.setVisibility(View.GONE);
                if (CardManager.getInstance().isAvaliable()) {
                    showErrorPage(false, R.string.wallet_disconnect_tips);
                } else if (CardManager.getInstance().isOverMaxQueryTimes()) {
                    showErrorPage(true, R.string.wallet_connect_timeout);
                } else {
                    showErrorPage(true, R.string.wallet_disconnect_tips);
                }
                mTrafficCardsFragment.reloadCards(forUpdateUI);
            }
        });
    }

    private void showErrorPage(boolean show, int text_id) {
        boolean isConnect = EnvManager.getInstance().isWatchConnected();
        mErrorView.setVisibility(isConnect ? View.GONE : View.VISIBLE);
        mLoading.setVisibility((show && isConnect) ? View.VISIBLE : View.GONE);
        if (text_id != 0) {
            mErrorTextView.setText(text_id);
        }
    }

    private boolean isModuleAvailable() {
        return Utils.isWalletMoubleEnable();
    }

    private void loadIntentIfNeed() {
        Intent fromIntent = getIntent();
        if (fromIntent == null || fromIntent.getScheme() == null) {
            return;
        }
        String urlData = fromIntent.getDataString();
        mIs3rdIssueCard = Constants.WALLET_BJISSUE_URI.equalsIgnoreCase(urlData);
    }

    private boolean isBeiJingIssueCardOk() {
        ICard card = CardManager.getInstance().getCard(CONFIG.BEIJINGTONG.mAID);
        if (card.getInstallStatus() != INSTALL_STATUS.PERSONAL) {
            return false;
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        if (mIs3rdIssueCard) {
            setResult(isBeiJingIssueCardOk() ? RESULT_OK : RESULT_CANCELED);
        }
        super.onBackPressed();
    }

    private void checkTestFunction(ActionBar actionBar) {
        if (!ServerHandler.getInstance(this).isTestEnv() && BranchUtil.isGA()) {
            return;
        }
        actionBar.getTitleView(false).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                System.arraycopy(mTitleViewHits, 1, mTitleViewHits, 0, mTitleViewHits.length - 1);
                mTitleViewHits[mTitleViewHits.length - 1] = SystemClock.uptimeMillis();
                if (mTitleViewHits[0] >= (SystemClock.uptimeMillis() - 1000)) {
                    startActivity(new Intent(WalletHomeActivity.this, TsmTestActivity.class));
                }
            }
        });
    }
}
