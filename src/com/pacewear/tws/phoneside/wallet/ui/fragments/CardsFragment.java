
package com.pacewear.tws.phoneside.wallet.ui.fragments;

import java.util.ArrayList;
import java.util.List;

import qrom.component.log.QRomLog;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.plugindemo.R;
import com.pacewear.tws.phoneside.wallet.card.CardManager;
import com.pacewear.tws.phoneside.wallet.card.ICard;
import com.pacewear.tws.phoneside.wallet.card.ICard.CARD_TYPE;
import com.pacewear.tws.phoneside.wallet.card.ICard.INSTALL_STATUS;
import com.pacewear.tws.phoneside.wallet.common.ButtonTouchStateListener;
import com.pacewear.tws.phoneside.wallet.common.Utils;
import com.pacewear.tws.phoneside.wallet.env.EnvManager;
import com.pacewear.tws.phoneside.wallet.order.IOrder;
import com.pacewear.tws.phoneside.wallet.order.OrderManager;
import com.pacewear.tws.phoneside.wallet.ui.ErrorCardActivity;
import com.pacewear.tws.phoneside.wallet.ui.SelectAddCardActivity;
import com.pacewear.tws.phoneside.wallet.ui.ShowCardDetailsActivity;
import com.pacewear.tws.phoneside.wallet.ui.ShowLoadingActivity;
import com.pacewear.tws.phoneside.wallet.ui.ShowWebPageActivity;
import com.pacewear.tws.phoneside.wallet.ui.widget.AddCard;
import com.pacewear.tws.phoneside.wallet.ui.widget.BaseCard;
import com.pacewear.tws.phoneside.wallet.ui.widget.EmptyCard;
import com.pacewear.tws.phoneside.wallet.ui.widget.TrafficCardView;
import com.tencent.tws.assistant.widget.AdapterView;
import com.tencent.tws.assistant.widget.AdapterView.OnItemClickListener;
import com.tencent.tws.assistant.widget.ListView;
import com.tencent.tws.assistant.widget.Toast;
import com.tencent.tws.pay.PayNFCConstants;

public class CardsFragment extends Fragment {

    private static final String TAG = PayNFCConstants.TAG + "."
            + CardsFragment.class.getSimpleName();

    private CARD_TYPE mType = CARD_TYPE.TRAFFIC_CARD;

    private Context mContext = null;

    private List<ICard> mListCard = new ArrayList<ICard>();

    private View mTuoWanCheckPasswdTip = null;

    private ListView mListView = null;

    private BaseAdapter mListAdapter = null;

    private EmptyCard mEmptyCard = null;

    public CardsFragment(CARD_TYPE type) {
        super();
        mType = type;
    }

    public boolean addCard() {
        Log.d(TAG, "addCard mType:" + mType);
        if (!CardManager.getInstance().isReady()) {
            Toast.makeText(mContext, R.string.wallet_sync_err_watch,
                    Toast.LENGTH_LONG).show();
            return false;
        }
        if (!OrderManager.getInstance().isOrderReady()) {
            Toast.makeText(mContext, R.string.wallet_sync_err_network,
                    Toast.LENGTH_LONG).show();
            return false;
        }
        if (!OrderManager.getInstance().isTrafficConfigReady()) {
            Toast.makeText(mContext, R.string.select_add_traffic_card_config_no_ready,
                    Toast.LENGTH_LONG).show();
            return false;
        }
        Intent intent = new Intent(mContext, SelectAddCardActivity.class);
        intent.putExtra(PayNFCConstants.ExtraKeyName.EXTRA_INT_CARDTYPE, mType);
        mContext.startActivity(intent);
        return true;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        mContext = getActivity();

        View view = inflater.inflate(R.layout.wallet_cards_list_container,
                container, false);

        mEmptyCard = (EmptyCard) view.findViewById(R.id.wallet_empty_card);
        mEmptyCard.setType(mType);
        mEmptyCard.setOnTouchListener(ButtonTouchStateListener.getInstance());
        mEmptyCard.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                addCard();
            }
        });

        TextView usinghelp = (TextView) view.findViewById(R.id.tv_usinghelp);
        usinghelp.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
        usinghelp.setOnTouchListener(ButtonTouchStateListener.getInstance());
        usinghelp.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Intent intent = new Intent(mContext, ShowWebPageActivity.class);
                ShowWebPageActivity.putTitle(intent,
                        getString(R.string.wallet_usinghelp_title));
                switch (mType) {
                    case TRAFFIC_CARD:
                        ShowWebPageActivity.putURL(intent, getString(
                                R.string.wallet_trafficcard_usinghelp_url));
                        break;
                    case BANK_CARD:
                        ShowWebPageActivity.putURL(intent,
                                getString(R.string.wallet_bankcard_usinghelp_url));
                        break;
                }
                mContext.startActivity(intent);
            }
        });

        mTuoWanCheckPasswdTip = view
                .findViewById(R.id.tuo_wan_check_passwd_tip);

        mListView = (ListView) view.findViewById(R.id.card_list);
        mListView.setDivider(null);

        mListAdapter = new BaseAdapter() {
            @Override
            public View getView(final int position, View convertView,
                    ViewGroup parent) {

                BaseCard card = null;

                if (convertView == null) {
                    if (position < mListCard.size()) {
                        switch (mType) {
                            case TRAFFIC_CARD:
                                convertView = new TrafficCardView(mContext);
                                break;
                            case BANK_CARD:
                                break;
                        }
                    } else {
                        convertView = new AddCard(mContext);
                    }
                }

                if (position < mListCard.size()) {
                    card = (BaseCard) convertView;
                    card.setCardsFragment(CardsFragment.this);
                    card.attachCard(mListCard.get(position));
                }
                convertView.setOnTouchListener(ButtonTouchStateListener.getInstance());
                convertView.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View arg0) {
                        onItemClickHandle(position);
                    }
                });
                return convertView;
            }

            @Override
            public int getViewTypeCount() {
                return 2;
            }

            @Override
            public int getItemViewType(int position) {
                if (position == mListCard.size()) {
                    return 1;
                }
                return 0;
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            @Override
            public Object getItem(int position) {
                return position;
            }

            @Override
            public int getCount() {

                int count = mListCard.size();

                // TODO
                // 视觉变来边去
                if (count != 0
                        && count < CardManager.getInstance().getCard(mType).size()) {
                    count += 1;
                }

                if (count != 0) {
                    mEmptyCard.setVisibility(View.GONE);
                    if (mType == CARD_TYPE.BANK_CARD) {
                        // TODO 判断脱腕检测密码是否打开
                        mTuoWanCheckPasswdTip.setVisibility(View.VISIBLE);
                    }
                } else {
                    mEmptyCard.setVisibility(View.VISIBLE);
                }

                return count;
            }
        };

        mListView.setAdapter(mListAdapter);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        reloadCards(false);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    public final void reloadCards(boolean isFinishSync) {
        mListCard.clear();
        List<ICard> list = CardManager.getInstance().getCard(mType);
        String whiteList = Utils.getCacheWhiteList();
        if (list != null && list.size() > 0) {
            int size = list.size();
            ICard card = null;
            IOrder order = null;
            for (int i = 0; i < size; i++) {
                card = list.get(i);
                order = OrderManager.getInstance().getLastOrder(card.getAID());
                if (!TextUtils.isEmpty(whiteList) && !whiteList.contains(card.getAID())) {
                    continue;
                }
                if (order != null
                        && (order.isIssueFail() || order.isCardTopFail())) {
                    mListCard.add(card);
                } else if (card.getInstallStatus() == INSTALL_STATUS.PERSONAL) {
                    mListCard.add(card);
                }
            }

            // 只有一张卡的时候不显示默认tag
            switch (mType) {
                case TRAFFIC_CARD:
                    TrafficCardView.setShowDefaultTag(
                            mListCard.size() == 1 ? false : true);
                    break;
                case BANK_CARD:
                    // BankCardView.setShowDefaultTag(
                    // mListCard.size() == 1 ? false : true);
                    break;
            }
        }

        mListAdapter.notifyDataSetChanged();
    }

    private void onItemClickHandle(int position) {
        if (position <= mListCard.size() - 1) {
            ICard card = mListCard.get(position);
            if (card == null) {
                return;
            }
            if (!EnvManager.getInstance().isWatchConnected()) {
                Toast.makeText(mContext, R.string.wallet_disconnect_tips, Toast.LENGTH_LONG).show();
                return;
            }
            if (!CardManager.getInstance().isReady()) {
                Toast.makeText(mContext, R.string.wallet_sync_err_watch, Toast.LENGTH_LONG).show();
                return;
            }
            IOrder order = OrderManager.getInstance()
                    .getLastOrder(card.getAID());
            if (order != null && order.isIssueFail()) {
                if (!isPayCardConfigOk(card)) {
                    Toast.makeText(mContext, R.string.select_add_traffic_card_config_no_ready,
                            Toast.LENGTH_LONG).show();
                    return;
                }
                ShowLoadingActivity.launchLoading(mContext,
                        card.getCardType(), card.getAID(),
                        order.getOrderReqParam().getEPayType(),
                        order.getOrderReqParam().getIOpenCardFee(),
                        order.getOrderReqParam().getITotalFee(),
                        ShowLoadingActivity.LOADING_TYPE_ACTIVATE_CARD,
                        true);
            } else if (order != null && order.isCardTopFail()) {
                if (!isPayCardConfigOk(card)) {
                    Toast.makeText(mContext, R.string.select_add_traffic_card_config_no_ready,
                            Toast.LENGTH_LONG).show();
                    return;
                }
                ShowLoadingActivity.launchLoading(mContext,
                        card.getCardType(), card.getAID(),
                        order.getOrderReqParam().getEPayType(),
                        order.getOrderReqParam().getITotalFee(),
                        ShowLoadingActivity.LOADING_TYPE_CHARGE_CARD,
                        true);
            } else if (!TextUtils.isEmpty(card.getCardInfoErrDesc())) {
                Intent intent = new Intent();
                intent.setClass(mContext,
                        ErrorCardActivity.class);
                intent.putExtra(
                        PayNFCConstants.ExtraKeyName.EXTRA_STR_INSTANCE_ID,
                        card.getAID());
                mContext.startActivity(intent);
            } else {
                // 正常情况，点击进入卡片详情
                Intent intent = new Intent();
                intent.setClass(mContext,
                        ShowCardDetailsActivity.class);
                intent.putExtra(
                        PayNFCConstants.ExtraKeyName.EXTRA_INT_CARDTYPE,
                        mType);
                intent.putExtra(
                        PayNFCConstants.ExtraKeyName.EXTRA_STR_INSTANCE_ID,
                        card.getAID());
                mContext.startActivity(intent);
            }
        } else {
            // Add card
            addCard();
        }
    }

    private boolean isPayCardConfigOk(ICard card) {
        if (OrderManager.getInstanceInner().getPayConfig(card.getAID()) == null) {
            return false;
        }
        return true;
    }
}
