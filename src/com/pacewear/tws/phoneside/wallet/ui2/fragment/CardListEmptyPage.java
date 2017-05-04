
package com.pacewear.tws.phoneside.wallet.ui2.fragment;

import com.pacewear.tws.phoneside.wallet.R;
import com.pacewear.tws.phoneside.wallet.present.ICardModulePresent;
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

public class CardListEmptyPage extends CardListFragment {
    private Button mIssueCardBtn = null;
    private ICardModulePresent mPresent = null;
    private Context mContext = null;

    public CardListEmptyPage(ICardModulePresent present) {
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
    protected boolean onUpdate() {
        // TODO Auto-generated method stub
        return false;
    }
}
