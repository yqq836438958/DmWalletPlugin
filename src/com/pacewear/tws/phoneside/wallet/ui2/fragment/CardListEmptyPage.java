
package com.pacewear.tws.phoneside.wallet.ui2.fragment;

import com.pacewear.tws.phoneside.wallet.R;
import com.pacewear.tws.phoneside.wallet.present.ICardListPresent;
import com.pacewear.tws.phoneside.wallet.ui2.activity.AddCardActivity;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;

public class CardListEmptyPage extends Fragment implements ICardListTypeView {
    private Button mIssueCardBtn = null;
    private ICardListPresent mPresent = null;
    private Context mContext = null;

    public CardListEmptyPage(ICardListPresent present) {
        super();
        mPresent = present;
        mContext = getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.wallet2_view_cardlist_empty,
                container, false);
        mIssueCardBtn = (Button) view.findViewById(R.id.wallet2_btn_issuecard);
        mIssueCardBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                jumAddCardActivity();
            }
        });
        return view;
    }

    private void jumAddCardActivity() {
        Intent it = new Intent(mContext, AddCardActivity.class);
        mContext.startActivity(it);
    }

    @Override
    public int update() {
        if (!mPresent.isCardListReady() || mPresent.size() > 0) {
            return -1;
        }
        return 0;
    }
}
