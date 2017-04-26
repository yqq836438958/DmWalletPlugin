
package com.pacewear.tws.phoneside.wallet.ui2.fragment;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.pacewear.tws.phoneside.wallet.R;
import com.pacewear.tws.phoneside.wallet.present.ICardListPresent;

public class CardListErrPage extends Fragment implements ICardListTypeView {
    private ICardListPresent mPresent;

    public CardListErrPage(ICardListPresent present) {
        super();
        init();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return null;
    }

    private void init() {
        inflate(getContext(), R.layout.wallet2_view_cardlist_empty, this);
        this.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                refreshPage();
            }
        });
    }

    private void refreshPage() {
        mPresent.cardListQuery();
    }

    @Override
    public int update() {
        if (mPresent.isCardListReady()) {
            setVisibility(View.GONE);
        } else {
            setVisibility(View.VISIBLE);
        }

    }
}
