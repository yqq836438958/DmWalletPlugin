
package com.pacewear.tws.phoneside.wallet.ui2.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.pacewear.tws.phoneside.wallet.R;
import com.pacewear.tws.phoneside.wallet.card.CardManager;
import com.pacewear.tws.phoneside.wallet.order.OrderManager;

public class CardListLoadingPage extends CardListFragment {
    public CardListLoadingPage() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.wallet2_view_cardlist_loading,
                container, false);
        return view;
    }

    @Override
    protected boolean onUpdate() {
        return CardManager.getInstance().isInSyncProcess()
                || OrderManager.getInstance().isInOrderSyncProcess();
    }

}
