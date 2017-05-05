
package com.pacewear.tws.phoneside.wallet.ui2.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.pacewear.tws.phoneside.wallet.R;
import com.pacewear.tws.phoneside.wallet.card.CardManager;
import com.pacewear.tws.phoneside.wallet.card.ICard;
import com.pacewear.tws.phoneside.wallet.card.ICard.CARD_TYPE;
import com.pacewear.tws.phoneside.wallet.card.ICard.INSTALL_STATUS;
import com.pacewear.tws.phoneside.wallet.common.ButtonTouchStateListener;
import com.pacewear.tws.phoneside.wallet.common.Utils;
import com.pacewear.tws.phoneside.wallet.order.IOrder;
import com.pacewear.tws.phoneside.wallet.order.OrderManager;
import com.pacewear.tws.phoneside.wallet.ui2.activity.AddCardActivity;
import com.pacewear.tws.phoneside.wallet.ui2.widget.BaseCardView;
import com.pacewear.tws.phoneside.wallet.ui2.widget.SimpleViewCache;
import com.pacewear.tws.phoneside.wallet.ui2.widget.TrafficCardView;

import java.util.ArrayList;
import java.util.List;

public class CardListPage extends CardListFragment {
    private RelativeLayout mAddNewCardLayout = null;
    private CardListAdapter mAdapter = null;
    private List<ICard> mListCard = new ArrayList<ICard>();
    private CARD_TYPE mType = CARD_TYPE.TRAFFIC_CARD;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.wallet2_view_cardlist_normal,
                container, false);
        ListView lView = (ListView) view.findViewById(R.id.listView);
        mAdapter = new CardListAdapter();
        lView.setAdapter(mAdapter);
        mAddNewCardLayout = (RelativeLayout) view.findViewById(R.id.cardlist_bottom);
        mAddNewCardLayout.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                goAddCardActivity();
            }
        });

        return view;
    }

    @Override
    protected boolean onUpdate() {
        return CardManager.getInstance().isReady();
    }

    @Override
    public void onResume() {
        super.onResume();
        updateCardListInternal();
    }

    private void updateCardListInternal() {
        mListCard.clear();
        List<ICard> list = CardManager.getInstance().getCard(mType);
        String whiteList = Utils.getCacheWhiteList();
        if (list != null && list.size() > 0) {
            int size = list.size();
            ICard card = null;
            IOrder order = null;
            for (int i = 0; i < size; i++) {
                card = list.get(i);
                order = OrderManager.getInstance().getLastOrder(card.getAID());
                if (!TextUtils.isEmpty(whiteList) && !whiteList.contains(card.getAID())) {
                    continue;
                }
                if (order != null
                        && (order.isIssueFail() || order.isCardTopFail())) {
                    mListCard.add(card);
                } else if (card.getInstallStatus() == INSTALL_STATUS.PERSONAL) {
                    mListCard.add(card);
                }
            }
        }

        if (mAdapter != null) {
            mAdapter.notifyDataSetChanged();
        }
    }

    class CardListAdapter extends BaseAdapter {
        @Override
        public View getView(final int position, View convertView,
                ViewGroup parent) {
            SimpleViewCache cache = null;
            if (convertView == null) {
                cache = new SimpleViewCache();
                convertView = new TrafficCardView(getActivity());
                cache.setBaseView(convertView);
                convertView.setTag(cache);
            } else {
                cache = (SimpleViewCache) convertView.getTag();
                convertView = cache.getBaseView();
            }
            TrafficCardView cardView = (TrafficCardView) convertView;
            cardView.attachCard(mListCard.get(position), BaseCardView.SENCE_LIST);
            convertView.setOnTouchListener(ButtonTouchStateListener.getInstance());
            return convertView;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public int getCount() {
            return mListCard.size();
        }
    }

    private void goAddCardActivity() {
        Intent intent = new Intent();
        intent.setClass(getActivity(), AddCardActivity.class);
        startActivity(intent);
    }
}
