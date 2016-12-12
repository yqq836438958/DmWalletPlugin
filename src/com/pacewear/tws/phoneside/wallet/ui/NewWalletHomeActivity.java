
package com.pacewear.tws.phoneside.wallet.ui;

import com.tencent.tws.assistant.app.ActionBar;
import com.tencent.tws.assistant.support.v4.view.ViewPager;
import com.tencent.tws.assistant.support.v4.view.ViewPager.OnPageChangeListener;
import com.tencent.tws.assistant.widget.TabIndicator;
import com.tencent.tws.assistant.widget.Toast;
import com.tencent.tws.framework.global.GlobalObj;
import com.tencent.tws.pay.PayNFCConstants;
import com.tencent.tws.phoneside.utils.BranchUtil;
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
import com.pacewear.tws.phoneside.wallet.ui.fragments.CardsFragment;
import com.pacewear.tws.phoneside.wallet.ui.handler.WalletHandlerManager;
import com.pacewear.tws.phoneside.wallet.ui.handler.WalletBaseHandler.ACTVITY_SCENE;
import com.pacewear.tws.phoneside.wallet.ui.handler.WalletBaseHandler.MODULE_CALLBACK;
import com.pacewear.tws.phoneside.wallet.ui.handler.WalletBaseHandler.OnWalletUICallback;

import android.app.Activity;
import android.app.LocalActivityManager;
import android.app.TwsActivity;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.TwsTabHost;
import android.widget.TwsTabWidget;
import android.widget.TwsTabHost.OnTabChangeListener;

public class NewWalletHomeActivity extends TwsActivity implements OnWalletUICallback {
    private static final String TAG = PayNFCConstants.TAG + "."
            + NewWalletHomeActivity.class.getSimpleName();

    private LocalActivityManager mLocalActivityManager = null;
    private TwsTabHost mTabHost;

    private String TAB_FIRST;
    private String TAB_SECOND;
    private static int TAB_FIRST_INDEX = 0;
    private static int TAB_SECOND_INDEX = 1;
    private int mCurrentTabIndex = TAB_FIRST_INDEX;
    private CardsFragment mTrafficCardsFragment = null;
    private CardsFragment mBankCardsFragment = null;
    private TabChangeListener mTabChangeListener = new TabChangeListener();
    private ViewPager mPager = null;
    private TabIndicator mIndicator = null;
    private View mLoading = null;
    private View mErrorView = null;
    private View mWaiting = null;
    private TextView mErrorTextView = null;
    private boolean mIs3rdIssueCard = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tab_content);
        initViews();
        onPostCreate();
        loadIntentIfNeed();
    }

    private void initViews() {
        TAB_FIRST = getString(R.string.nfc_traffic_card);
        TAB_SECOND = getString(R.string.nfc_bank_card);

        TwsTabWidget tabWidget = (TwsTabWidget) findViewById(R.id.tabs);
        tabWidget.setBackgroundResource(R.color.wallet_action_bar_background);

        mTabHost = (TwsTabHost) findViewById(R.id.tabhost);
        prepareTabViewAndActionBar();
        mWaiting = findViewById(R.id.wallet_main_wait);
        if (!isModuleAvailable()) {
            mWaiting.setVisibility(View.VISIBLE);
            return;
        }
        mTabHost.setCurrentTab(mCurrentTabIndex);
        mPager = (ViewPager) findViewById(R.id.tabviewpager);
        mIndicator = (TabIndicator) findViewById(R.id.tab_indicator);
        ViewPager.OnPageChangeListener mChangeListener = new OnPageChangeListener() {

            @Override
            public void onPageSelected(int position) {
            }

            @Override
            public void onPageScrolled(int position, float positionOffset,
                    int positionOffsetPixels) {
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        };
        mTabHost.setOnPageChangeListener(mChangeListener);
        mTabHost.setOnTabChangedListener(new OnTabChangeListener() {
            @Override
            public void onTabChanged(String arg0) {
            }
        });
        mLoading = findViewById(R.id.wallet_main_loading_ly);
        mErrorView = findViewById(R.id.wallet_main_error);
        mErrorTextView = (TextView) findViewById(R.id.wallet_main_error_tv);
        if (!CardManager.getInstance().isAvaliable()) {
            showErrorPage(true, 0);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        WalletHandlerManager.getInstance().requestFocus(ACTVITY_SCENE.SCENE_SYNCALL);
        mTrafficCardsFragment.onResume();
        // mBankCardsFragment.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mTrafficCardsFragment.onPause();
        // mBankCardsFragment.onPause();
    }

    @Override
    protected void onDestroy() {
        WalletHandlerManager.getInstance().unregister(ACTVITY_SCENE.SCENE_SYNCALL);
        super.onDestroy();
    }

    private void prepareTabViewAndActionBar() {

        ActionBar actionBar = getTwsActionBar();
        actionBar.setBackgroundDrawable(new ColorDrawable(getResources()
                .getColor(R.color.wallet_action_bar_background)));
        if (mTabHost != null) {
//            FragmentManager
            mTabHost.setup(mCurrentTabIndex);

            String tabTitle = TAB_FIRST;
            mTrafficCardsFragment = new CardsFragment(
                    CARD_TYPE.TRAFFIC_CARD);
            mTabHost.addTab(mTabHost.newTabSpec(TAB_FIRST)
                    .setIndicator(tabTitle).setContent(mTrafficCardsFragment));

            // tabTitle = TAB_SECOND;
            // mBankCardsFragment = new CardsFragment(
            // CARD_TYPE.BANK_CARD);
            // mTabHost.addTab(mTabHost.newTabSpec(TAB_SECOND)
            // .setIndicator(tabTitle).setContent(mBankCardsFragment));
            //
            // mTabHost.setOnTabChangedListener(mTabChangeListener);
        }
    }

    private class TabChangeListener implements OnTabChangeListener {

        @Override
        public void onTabChanged(String tabId) {
            if (tabId == TAB_FIRST) {
                mCurrentTabIndex = TAB_FIRST_INDEX;
                mTrafficCardsFragment.onResume();
            } else if (tabId == TAB_SECOND) {
                mCurrentTabIndex = TAB_SECOND_INDEX;
                // mBankCardsFragment.onResume();
            }
        }
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
}
