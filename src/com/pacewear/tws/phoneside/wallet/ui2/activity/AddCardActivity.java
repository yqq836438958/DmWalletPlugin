
package com.pacewear.tws.phoneside.wallet.ui2.activity;

import android.app.Activity;
import android.app.TwsActivity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

import com.pacewear.tws.phoneside.wallet.R;
import com.pacewear.tws.phoneside.wallet.card.CardManager;
import com.pacewear.tws.phoneside.wallet.card.ICard;
import com.pacewear.tws.phoneside.wallet.card.ICard.CARD_TYPE;
import com.pacewear.tws.phoneside.wallet.card.ICard.INSTALL_STATUS;
import com.pacewear.tws.phoneside.wallet.card.ICardInner.CONFIG;
import com.pacewear.tws.phoneside.wallet.common.Utils;
import com.pacewear.tws.phoneside.wallet.env.EnvManager;
import com.pacewear.tws.phoneside.wallet.order.IOrder;
import com.pacewear.tws.phoneside.wallet.order.OrderManager;
import com.pacewear.tws.phoneside.wallet.ui.ShowWebPageActivity;
import com.pacewear.tws.phoneside.wallet.ui.widget.SimpleCardListItem;
import com.pacewear.tws.phoneside.wallet.ui2.toast.WalletErrToast;
import com.tencent.tws.assistant.app.ActionBar;
import com.tencent.tws.pay.PayNFCConstants;
import com.tencent.tws.phoneside.phoneverify.PhoneVerifyActivity;

import java.util.ArrayList;
import java.util.List;

public class AddCardActivity extends TwsActivity {
    public static final String TAG = AddCardActivity.class.getSimpleName();

    private CARD_TYPE mType = CARD_TYPE.TRAFFIC_CARD;

    private Context mContext = null;

    private List<ICard> mCards = new ArrayList<ICard>();

    private ListView mListView = null;

    private BaseAdapter mListAdapter = null;

    private final int VERIFYREQUEST = 1;
    private String mPendingCardAid = "";

    @Override
    public void onCreate(Bundle savedInstanceState) {

        // overridePendingTransition(R.anim.wallet_push_up, 0);

        super.onCreate(savedInstanceState);

        mContext = this;

        Intent intent = getIntent();
        if (intent != null) {
            int _type = intent
                    .getIntExtra(PayNFCConstants.ExtraKeyName.EXTRA_INT_CARDTYPE, 0);
            mType = CARD_TYPE.values()[_type];
        }

        setContentView(R.layout.wallet_select_add_card);
        mListView = (ListView) findViewById(R.id.card_list);

        TextView tip = (TextView) findViewById(R.id.wallet_select_add_card_tip);

        ActionBar actionBar = getTwsActionBar();
        switch (mType) {
            case TRAFFIC_CARD:
                actionBar.setTitle(getString(R.string.select_add_traffic_card_title));
                tip.setText(R.string.select_add_traffic_card_tip);
                break;
            case BANK_CARD:
                actionBar.setTitle(getString(R.string.select_add_bank_card_title));
                tip.setText(R.string.select_add_bank_card_tip);
                break;
            default:
                return;
        }

        Button actionLeftBt = (Button) actionBar.getCloseView(false);
        actionLeftBt.setText(getResources().getString(R.string.wallet_select_default_cancel));
        actionLeftBt.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                finish();
            }
        });

        ArrayList<ICard> cards = CardManager.getInstanceInner().getCard(mType);

        if (cards == null) {
            return;
        }

        ICard card = null;
        IOrder order = null;
        String whiteList = Utils.getCacheWhiteList();
        for (int i = 0; i < cards.size(); i++) {
            card = cards.get(i);
            if (!TextUtils.isEmpty(whiteList) && !whiteList.contains(card.getAID())) {
                continue;
            }
            order = OrderManager.getInstance().getLastOrder(card.getAID());
            if (order != null && (order.isIssueFail() || order.isCardTopFail())) {
                continue;
            }
            if (card.getInstallStatus() == INSTALL_STATUS.UNINSTALLED
                    || card.getInstallStatus() == INSTALL_STATUS.INSTALLED) {
                mCards.add(card);
            }
        }
        mListAdapter = new BaseAdapter() {
            @Override
            public int getCount() {
                if (mCards != null) {
                    return mCards.size();
                }
                return 0;
            }

            @Override
            public Object getItem(int position) {
                return position;
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {

                SimpleCardListItem item = null;

                if (convertView == null) {
                    convertView = new SimpleCardListItem(mContext);
                }

                item = (SimpleCardListItem) convertView;
                item.attachCard(mCards.get(position));

                return item;
            }
        };

        mListView.setAdapter(mListAdapter);
        mListView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {

                ICard newCard = mCards.get(position);
                if (gotoPreOpenCardPage(newCard.getAID())) {
                    return;
                }
                Intent intent = null;
                switch (newCard.getCardType()) {
                    case TRAFFIC_CARD:
                        if (WalletErrToast.checkAll(AddCardActivity.this)) {
                            return;
                        }
                        intent = new Intent(mContext, CardIssuePrepareActivity.class);
                        break;
                    case BANK_CARD:
                        // TODO
                        break;
                }
                intent.putExtra(PayNFCConstants.ExtraKeyName.EXTRA_INT_CARDTYPE,
                        newCard.getCardType().toValue());
                intent.putExtra(PayNFCConstants.ExtraKeyName.EXTRA_STR_INSTANCE_ID,
                        newCard.getAID());
                mContext.startActivity(intent);

                finish();
            }
        });
    }

    private boolean gotoPreOpenCardPage(String aid) {
        mPendingCardAid = aid;
        if (TextUtils.isEmpty(EnvManager.getInstanceInner().getUserPhoneNum())) {// todo
            Intent intent = new Intent(mContext, PhoneVerifyActivity.class);
            startActivityForResult(intent, VERIFYREQUEST);
            return true;
        }
        return false;
    }

    private boolean goOtherPage(String aid) {
        if (CONFIG.LINGNANTONG.mAID.equalsIgnoreCase(aid)) {
            Intent intent = new Intent(mContext, ShowWebPageActivity.class);
            ShowWebPageActivity.putTitle(intent, getString(R.string.wallet_agreement_title));
            ShowWebPageActivity.putURL(intent,
                    getString(R.string.wallet_lnt_agreement_rl));
            ShowWebPageActivity.putActionBarText(intent, getString(R.string.wallet_cancel),
                    getString(R.string.wallet_confirm));
            intent.putExtra(ShowWebPageActivity.TARGET_CLASS, CardIssuePrepareActivity.class.getName());
            intent.putExtra(ShowWebPageActivity.TARGET_AID, aid);
            startActivity(intent);
            finish();
            return true;
        }
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (resultCode) {
            case Activity.RESULT_OK:
                String num = data.getStringExtra(PhoneVerifyActivity.PHONENUM);
                EnvManager.getInstanceInner().setUserPhoneNum(num);
                if (requestCode == VERIFYREQUEST) {
                    if (goOtherPage(mPendingCardAid)) {
                        return;
                    }
                    Intent intent = new Intent();
                    intent.setClass(this, CardIssuePrepareActivity.class);
                    intent.putExtra(PayNFCConstants.ExtraKeyName.EXTRA_STR_INSTANCE_ID,
                            mPendingCardAid);
                    startActivity(intent);
                    finish();
                }
                break;
            default:
                break;
        }
    }
}
