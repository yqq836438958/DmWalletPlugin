
package com.pacewear.tws.phoneside.wallet.ui2.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.pacewear.tws.phoneside.wallet.R;
import com.pacewear.tws.phoneside.wallet.common.FontsOverride;
import com.pacewear.tws.phoneside.wallet.common.Utils;
import com.pacewear.tws.phoneside.wallet.transaction.CardTransactItem;

import java.util.ArrayList;

public class TransactionView extends RelativeLayout {
    private TransactionAdapter mListAdapter = null;
    private ListView mListView = null;
    private View mLoadingView = null;
    private TextView mEmptyView;
    private final int STAT_LOADING = 0;
    private final int STAT_RET_NULL = 1;
    private final int STAT_RET_VALID = 2;

    public TransactionView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        LayoutInflater.from(context).inflate(R.layout.wallet2_cardtransact_views, this);
        initViews();
        init(context);
    }

    private void init(Context ctx) {
        mListAdapter = new TransactionAdapter(ctx);
        mListView.setAdapter(mListAdapter);
        updateUI(STAT_LOADING);
    }

    public void fillData(ArrayList<CardTransactItem> list) {
        updateUI((list == null) ? STAT_RET_NULL : STAT_RET_VALID);
        mListAdapter.refresh(list);
    }

    public TransactionView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TransactionView(Context context) {
        this(context, null);
    }

    private void updateUI(int status) {
        boolean isLoading = (status == STAT_LOADING);
        mEmptyView.setVisibility(!isLoading ? View.VISIBLE : View.GONE);
        mListView.setVisibility(!isLoading ? View.VISIBLE : View.GONE);
        mLoadingView.setVisibility(isLoading ? View.VISIBLE : View.GONE);
    }

    private void initViews() {
        mListView = (ListView) findViewById(R.id.wallet_lv_transact);
        mEmptyView = (TextView) findViewById(R.id.wallet_tv_transact_empty);
        mLoadingView = findViewById(R.id.wallet_transact_loading);
        mListView.setEmptyView(mEmptyView);
    }

    class TransactionAdapter extends BaseAdapter {
        private Context mContext = null;
        private ArrayList<CardTransactItem> mDataList = null;

        public TransactionAdapter(Context ctx) {
            mContext = ctx;
        }

        public void refresh(ArrayList<CardTransactItem> list) {
            mDataList = list;
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return (mDataList != null && mDataList.size() > 0) ? mDataList.size() : 0;
        }

        @Override
        public Object getItem(int arg0) {
            return null;
        }

        @Override
        public long getItemId(int arg0) {
            return 0;
        }

        @Override
        public View getView(int postion, View contentView, ViewGroup viewGroup) {
            ViewHolder holder = null;
            if (contentView == null) {
                holder = new ViewHolder();
                contentView = LayoutInflater.from(mContext)
                        .inflate(R.layout.wallet2_cardtransact_item, viewGroup, false);
                holder.tvTime = (TextView) contentView.findViewById(R.id.wallet_tv_time);
                holder.tvType = (TextView) contentView.findViewById(R.id.wallet_tv_type);
                holder.tvAmount = (TextView) contentView.findViewById(R.id.wallet_tv_amount);
                holder.tvAmount.setTypeface(FontsOverride.getDigitFont(mContext));
                contentView.setTag(holder);
            } else {
                holder = (ViewHolder) contentView.getTag();
            }
            CardTransactItem data = mDataList.get(postion);
            boolean isTopup = (data.iType == 1);
            holder.tvTime.setText(data.strTime);
            holder.tvAmount.setText(getBalanceDesc(isTopup, data.lAmount));
            holder.tvType.setText(isTopup ? R.string.wallet_transact_topup
                    : R.string.wallet_transact_consume);
            return contentView;
        }

        class ViewHolder {
            TextView tvTime;
            TextView tvType;
            TextView tvAmount;
        }

        private String getBalanceDesc(boolean isTopup, long amount) {
            StringBuilder ret = new StringBuilder();
            ret = ret.append(isTopup ? "+" : "-").append("¥")
                    .append(Utils.getDisplayBalance(amount));
            return ret.toString();
        }
    }
}
