
package com.pacewear.tws.phoneside.wallet.ui2.activity;

import android.app.TwsActivity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;

import com.pacewear.tws.phoneside.wallet.R;
import com.pacewear.tws.phoneside.wallet.WalletApp;
import com.pacewear.tws.phoneside.wallet.card.CardManager;
import com.pacewear.tws.phoneside.wallet.card.ICard;
import com.pacewear.tws.phoneside.wallet.card.ICard.ACTIVATION_STATUS;
import com.pacewear.tws.phoneside.wallet.card.ICard.INSTALL_STATUS;
import com.pacewear.tws.phoneside.wallet.ui.handler.WalletHandlerManager;
import com.pacewear.tws.phoneside.wallet.ui.handler.WalletBaseHandler.ACTVITY_SCENE;
import com.pacewear.tws.phoneside.wallet.ui.handler.WalletBaseHandler.MODULE_CALLBACK;
import com.pacewear.tws.phoneside.wallet.ui.handler.WalletBaseHandler.OnWalletUICallback;
import com.pacewear.tws.phoneside.wallet.ui.widget.SimpleCardListItem;
import com.tencent.tws.assistant.app.ActionBar;
import com.tencent.tws.assistant.widget.Toast;

import java.util.ArrayList;
import java.util.List;

// plan 2.0
public class SetDefaultActivity extends TwsActivity implements OnWalletUICallback {
    private CardListAdapter mCardListAdapter = null;
    private boolean isLoading = false;
    private int mLastClickIndex = -1;

    @Override
    protected void onCreate(Bundle args) {
        super.onCreate(args);
        setContentView(R.layout.wallet2_activity_setdefault);
        ListView listView = (ListView) findViewById(R.id.lv_cardlist);
        mCardListAdapter = new CardListAdapter(getPersonalCards());
        listView.setAdapter(mCardListAdapter);
        ActionBar actionBar = getTwsActionBar();
        actionBar.setTitle(R.string.wallet_set_default_card);
        WalletHandlerManager.getInstance().register("", ACTVITY_SCENE.SCENE_SWITCHCARD, this);
    }

    @Override
    public void onResume() {
        super.onResume();
        WalletHandlerManager.getInstance().requestFocus(ACTVITY_SCENE.SCENE_SWITCHCARD);
    }

    private List<ICard> getPersonalCards() {
        return CardManager.getInstance().getCard(INSTALL_STATUS.PERSONAL);
    }

    class CardListAdapter extends BaseAdapter {
        private List<ICard> mList = null;
        private SimpleItemRightResStagy mStagy = null;

        public CardListAdapter(List<ICard> list) {
            mList = list;
            mStagy = new SimpleItemRightResStagy();
        }

        @Override
        public int getCount() {
            return mList != null ? mList.size() : 0;
        }

        @Override
        public ICard getItem(int position) {
            return mList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            SimpleCardListItem item = null;

            if (convertView == null) {
                convertView = new SimpleCardListItem(SetDefaultActivity.this);
            }
            final ICard card = mList.get(position);
            item = (SimpleCardListItem) convertView;
            item.attachCard(card);
            mStagy.call(item, card, position);
            item.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (card.getActivationStatus() == ACTIVATION_STATUS.ACTIVATED || isLoading) {
                        return;
                    }
                    isLoading = true;
                    mLastClickIndex = position;
                    CardListAdapter.this.notifyDataSetChanged();
                    CardManager.getInstance().setDefaultCard(card.getAID());
                }
            });
            return item;
        }

        private void hideAllItemRightRes(SimpleCardListItem item) {
            item.hideItemLoading();
            item.setRightBitmap(0);
        }
    }

    @Override
    public void onUIUpdate(MODULE_CALLBACK module, int ret, boolean forUpdateUI) {
        this.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                // if (mLoading.getVisibility() == View.VISIBLE && ret != 0) {
                // Toast.makeText(WalletApp.getHostAppContext(),
                // getString(R.string.wallet_set_default_dev_not_connected),
                // Toast.LENGTH_LONG).show();
                // }
                // TODO ADD mLoading code
                isLoading = false;
                mCardListAdapter.notifyDataSetChanged();
            }
        });
    }

    interface IItemRightResStagy {
        public void onHandle(SimpleCardListItem item);

        public boolean isReady(int positon, ICard card);
    }

    private boolean hasClickItem(int pos) {
        return mLastClickIndex == pos;
    }

    private boolean isTargeDefCard(ICard card) {
        return (card.getActivationStatus() == ACTIVATION_STATUS.ACTIVATED);
    }

    class SimpleItemRightResStagy {
        private List<IItemRightResStagy> showList = new ArrayList<SetDefaultActivity.IItemRightResStagy>();

        public SimpleItemRightResStagy() {
            showList.add(new LoadingClickShow());
            showList.add(new NormalDefaultShow());
            showList.add(new NormalOtherShow());
        }

        public void call(SimpleCardListItem item, ICard card, int position) {
            for (IItemRightResStagy stagy : showList) {
                if (stagy.isReady(position, card)) {
                    stagy.onHandle(item);
                    break;
                }
            }
        }
    }

    class NormalDefaultShow implements IItemRightResStagy {

        @Override
        public void onHandle(SimpleCardListItem item) {
            item.setItemSelect(true);
        }

        @Override
        public boolean isReady(int positon, ICard card) {
            return !isLoading && isTargeDefCard(card);
        }
    }

    class NormalOtherShow implements IItemRightResStagy {

        @Override
        public void onHandle(SimpleCardListItem item) {
            item.setRightBitmap(0);
        }

        @Override
        public boolean isReady(int positon, ICard card) {
            return true;
        }
    }

    class LoadingClickShow implements IItemRightResStagy {

        @Override
        public void onHandle(SimpleCardListItem item) {
            item.showItemLoading();
        }

        @Override
        public boolean isReady(int positon, ICard card) {
            return isLoading && hasClickItem(positon);
        }
    }

}
