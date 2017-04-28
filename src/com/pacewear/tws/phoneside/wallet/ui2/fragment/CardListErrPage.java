
package com.pacewear.tws.phoneside.wallet.ui2.fragment;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import com.pacewear.tws.phoneside.wallet.R;
import com.pacewear.tws.phoneside.wallet.card.CardManager;

public class CardListErrPage extends CardListFragment {

    public CardListErrPage() {
        super();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.wallet2_view_cardlist_error,
                container, false);
        view.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                CardManager.getInstance().forceUpdate(true);
            }
        });
        return view;
    }

    @Override
    protected boolean onUpdate() {
        if (!CardManager.getInstance().isAvaliable()) {
            return true;
        }
        return false;
    }
}
