
package com.pacewear.tws.phoneside.wallet.ui;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.TwsActivity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.pacewear.tws.phoneside.wallet.R;
import com.pacewear.tws.phoneside.wallet.card.CardManager;
import com.pacewear.tws.phoneside.wallet.card.ICard;
import com.pacewear.tws.phoneside.wallet.card.ICard.CARD_TYPE;
import com.pacewear.tws.phoneside.wallet.card.ICard.INSTALL_STATUS;
import com.pacewear.tws.phoneside.wallet.card.ICardInner.CONFIG;
import com.pacewear.tws.phoneside.wallet.env.EnvManager;
import com.pacewear.tws.phoneside.wallet.order.IOrder;
import com.pacewear.tws.phoneside.wallet.order.OrderManager;
import com.pacewear.tws.phoneside.wallet.ui.widget.SimpleCardListItem;
import com.tencent.tws.assistant.app.ActionBar;
import com.tencent.tws.assistant.widget.AdapterView;
import com.tencent.tws.assistant.widget.AdapterView.OnItemClickListener;
import com.tencent.tws.assistant.widget.ListView;
import com.tencent.tws.pay.PayNFCConstants;

public class SelectAddCardActivity extends TwsActivity {

    public static final String TAG = SelectAddCardActivity.class.getSimpleName();

    private CARD_TYPE mType = CARD_TYPE.TRAFFIC_CARD;

    private Context mContext = null;

    private List<ICard> mCards = new ArrayList<ICard>();

    private ListView mListView = null;

    private BaseAdapter mListAdapter = null;

    private final int PEKINGREQUEST = 1;

    @Override
    public void finish() {
        super.finish();
        // overridePendingTransition(0, R.anim.wallet_push_down);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        // overridePendingTransition(R.anim.wallet_push_up, 0);

        super.onCreate(savedInstanceState);

        mContext = this;

        Intent intent = getIntent();
        if (intent != null) {
            mType = (CARD_TYPE) intent
                    .getSerializableExtra(PayNFCConstants.ExtraKeyName.EXTRA_INT_CARDTYPE);
        }

        setContentView(R.layout.wallet_select_add_card);
        mListView = (ListView) findViewById(R.id.card_list);

        TextView tip = (TextView) findViewById(R.id.wallet_select_add_card_tip);

        ActionBar actionBar = getTwsActionBar();
        actionBar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(
                R.color.wallet_action_bar_background)));
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
        for (int i = 0; i < cards.size(); i++) {
            card = cards.get(i);
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
                        if (OrderManager.getInstanceInner().getPayConfig(newCard.getAID()) == null
                                || !OrderManager.getInstance().isOrderReady()) {
                            Toast.makeText(mContext,
                                    R.string.select_add_traffic_card_config_no_ready,
                                    Toast.LENGTH_LONG).show();
                            return;
                        }
                        if (!CardManager.getInstance().isReady()) {
                            Toast.makeText(mContext, R.string.wallet_sync_err_watch,
                                    Toast.LENGTH_LONG).show();
                            return;
                        }
                        intent = new Intent(mContext, ActivateCardActivity.class);
                        break;
                    case BANK_CARD:
                        // TODO
                        intent = new Intent(mContext, ActivateCardActivity.class);
                        break;
                }
                intent.putExtra(PayNFCConstants.ExtraKeyName.EXTRA_INT_CARDTYPE,
                        newCard.getCardType());
                intent.putExtra(PayNFCConstants.ExtraKeyName.EXTRA_STR_INSTANCE_ID,
                        newCard.getAID());
                mContext.startActivity(intent);

                finish();
            }
        });
    }

    private boolean gotoPreOpenCardPage(String aid) {
        if (CONFIG.LINGNANTONG.mAID.equalsIgnoreCase(aid)) {
            Intent intent = new Intent(mContext, ShowWebPageActivity.class);
            ShowWebPageActivity.putTitle(intent, getString(R.string.wallet_agreement_title));
            ShowWebPageActivity.putURL(intent,
                    getString(R.string.wallet_lnt_agreement_rl));
            ShowWebPageActivity.putActionBarText(intent, getString(R.string.wallet_cancel),
                    getString(R.string.wallet_confirm));
            intent.putExtra(ShowWebPageActivity.TARGET_CLASS, ActivateCardActivity.class.getName());
            intent.putExtra(ShowWebPageActivity.TARGET_AID, aid);
            startActivity(intent);
            finish();
            return true;
        }
        if (CONFIG.BEIJINGTONG.mAID.equalsIgnoreCase(aid)
                && TextUtils.isEmpty(EnvManager.getInstanceInner().getUserPhoneNum())) {// todo
            // Intent intent = new Intent(mContext, PhoneVerifyActivity.class);
            // startActivityForResult(intent, PEKINGREQUEST);
            // finish();
            return true;
        }
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (resultCode) {
            case Activity.RESULT_OK:
                String num = null;// data.getStringExtra(PhoneVerifyActivity.PHONENUM);
                EnvManager.getInstanceInner().setUserPhoneNum(num);
                if (requestCode == PEKINGREQUEST) {
                    Intent intent = new Intent();
                    intent.setClass(this, ActivateCardActivity.class);
                    intent.putExtra(PayNFCConstants.ExtraKeyName.EXTRA_STR_INSTANCE_ID,
                            CONFIG.BEIJINGTONG.mAID);
                    startActivity(intent);
                    finish();
                }
                break;
            default:
                break;
        }
    }
}
