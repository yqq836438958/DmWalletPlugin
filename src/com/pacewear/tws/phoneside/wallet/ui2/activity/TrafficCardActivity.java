
package com.pacewear.tws.phoneside.wallet.ui2.activity;

import android.app.TwsActivity;
import android.content.Intent;
import android.os.Bundle;

import com.pacewear.tws.phoneside.wallet.R;
import com.pacewear.tws.phoneside.wallet.card.CardManager;
import com.pacewear.tws.phoneside.wallet.card.ICard;
import com.pacewear.tws.phoneside.wallet.card.ICard.INSTALL_STATUS;
import com.pacewear.tws.phoneside.wallet.card.ICardInner.CONFIG;
import com.pacewear.tws.phoneside.wallet.common.Constants;
import com.pacewear.tws.phoneside.wallet.env.EnvManager;
import com.pacewear.tws.phoneside.wallet.order.OrderManager;
import com.pacewear.tws.phoneside.wallet.ui.handler.WalletHandlerManager;
import com.pacewear.tws.phoneside.wallet.ui.handler.WalletBaseHandler.ACTVITY_SCENE;
import com.pacewear.tws.phoneside.wallet.ui.handler.WalletBaseHandler.MODULE_CALLBACK;
import com.pacewear.tws.phoneside.wallet.ui.handler.WalletBaseHandler.OnWalletUICallback;
import com.pacewear.tws.phoneside.wallet.ui2.fragment.CardListErrPage;
import com.pacewear.tws.phoneside.wallet.ui2.fragment.CardListFragment;
import com.pacewear.tws.phoneside.wallet.ui2.fragment.CardListPage;
import com.pacewear.tws.phoneside.wallet.ui2.fragment.CardListFragment.FragmentController;
import com.tencent.tws.assistant.app.ActionBar;
import com.pacewear.tws.phoneside.wallet.ui2.fragment.CardListLoadingPage;
import com.pacewear.tws.phoneside.wallet.ui2.fragment.CardListNotSupportPage;

import java.util.ArrayList;
import java.util.List;

public class TrafficCardActivity extends TwsActivity implements OnWalletUICallback {
    private FragmentController mController = null;
    private boolean mIs3rdIssueCard = false;

    @Override
    public void onCreate(Bundle args) {
        super.onCreate(args);
        setContentView(R.layout.wallet2_home);
        init();
        WalletHandlerManager.getInstance().requestFocus(ACTVITY_SCENE.SCENE_SYNCALL);
        onPostCreate();
        loadIntentIfNeed();
    }

    @Override
    protected void onDestroy() {
        WalletHandlerManager.getInstance().unregister(ACTVITY_SCENE.SCENE_SYNCALL);
        super.onDestroy();
    }

    @Override
    public void onUIUpdate(final MODULE_CALLBACK module, final int ret, final boolean forUpdateUI) {
        this.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                mController.update();
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (mIs3rdIssueCard) {
            setResult(isBeiJingIssueCardOk() ? RESULT_OK : RESULT_CANCELED);
        }
        super.onBackPressed();
    }

    private void init() {
        ActionBar actionBar = getTwsActionBar();
        actionBar.setTitle(R.string.nfc_traffic_card);
        List<CardListFragment> list = new ArrayList<CardListFragment>();
        list.add(new CardListNotSupportPage());
        list.add(new CardListLoadingPage());
        list.add(new CardListErrPage());
        list.add(new CardListPage());
        mController = new FragmentController(this, R.id.wallet_content, list);
        mController.update();
    }

    private void loadIntentIfNeed() {
        Intent fromIntent = getIntent();
        if (fromIntent == null /* || fromIntent.getScheme() == null */) {
            return;
        }
        String urlData = fromIntent.getDataString();
        mIs3rdIssueCard = Constants.WALLET_BJISSUE_URI.equalsIgnoreCase(urlData);
    }

    private void onPostCreate() {
        EnvManager.getInstance().forceSyncCPLC(false);
        CardManager.getInstance().forceUpdate(false);
        OrderManager.getInstance().forceSyncTrafficConfig(true);
        OrderManager.getInstance().forceSyncOrder(true);
    }

    private boolean isBeiJingIssueCardOk() {
        ICard card = CardManager.getInstance().getCard(CONFIG.BEIJINGTONG.mAID);
        if (card.getInstallStatus() != INSTALL_STATUS.PERSONAL) {
            return false;
        }
        return true;
    }
}
