
package com.pacewear.tws.phoneside.wallet.ui2.fragment;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.pacewear.tws.phoneside.wallet.R;
import com.pacewear.tws.phoneside.wallet.present.ICardListPresent;

public class CardListErrPage extends RelativeLayout implements ICardListTypeView {
    private ICardListPresent mPresent;

    public CardListErrPage(ICardListPresent present) {
        super();
        init();
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
    public void update() {
        if (mPresent.isCardListReady()) {
            setVisibility(View.GONE);
        } else {
            setVisibility(View.VISIBLE);
        }

    }
}
