
package com.pacewear.tws.phoneside.wallet.ui2.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.pacewear.tws.phoneside.wallet.R;
import com.pacewear.tws.phoneside.wallet.common.Utils;

public class CardListNotSupportPage extends CardListFragment {

    public CardListNotSupportPage() {
        super();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.wallet2_view_cardlist_notsupport,
                container, false);
        return view;
    }

    @Override
    protected boolean onUpdate() {
        if (!Utils.isWalletMoubleEnable()) {
            return true;
        }
        return false;
    }
}
