
package com.pacewear.tws.phoneside.wallet.ui2.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.pacewear.tws.phoneside.wallet.R;
import com.pacewear.tws.phoneside.wallet.card.CardManager;
import com.pacewear.tws.phoneside.wallet.env.EnvManager;
import com.pacewear.tws.phoneside.wallet.order.OrderManager;

public class CardListNoConnectPage extends CardListFragment {
    public CardListNoConnectPage() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.wallet2_view_cardlist_noconnect,
                container, false);
        return view;
    }

    @Override
    protected void onUpdate() {
    }

    @Override
    protected boolean isReady() {
        return !EnvManager.getInstance().isWatchConnected();
    }

}
