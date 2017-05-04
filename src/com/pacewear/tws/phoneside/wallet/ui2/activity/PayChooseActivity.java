
package com.pacewear.tws.phoneside.wallet.ui2.activity;

import android.app.Activity;
import android.app.TwsActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import com.pacewear.tws.phoneside.wallet.R;
import com.pacewear.tws.phoneside.wallet.common.Utils;
import com.pacewear.tws.phoneside.wallet.pay.PayBean;
import com.pacewear.tws.phoneside.wallet.pay.PayManager;
import com.pacewear.tws.phoneside.wallet.ui.widget.SimpleCardListItem;
import com.pacewear.tws.phoneside.wallet.ui2.widget.SimpleViewCache;

import java.util.List;

public class PayChooseActivity extends TwsActivity {
    private int mSelectPayType = 0;
    public static final String PAY_TYPE = "pay_type";
    public static final String PAY_AMOUNT = "pay_amount";
    public static final String PAY_DESC = "pay_desc";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wallet2_activity_paychoose);
        Intent intent = getIntent();
        ListView payList = (ListView) findViewById(R.id.lv_paychoose_list);
        Button confirm = (Button) findViewById(R.id.btn_paychoose_confirm);
        TextView payMoney = (TextView) findViewById(R.id.tv_paychoose_money);
        TextView desc = (TextView) findViewById(R.id.tv_paychoose_desc);
        final PaySelectAdapter paySelectAdapter = new PaySelectAdapter(
                PayManager.getInstanceInner().getPayBeans());
        payList.setAdapter(paySelectAdapter);
        final long lAmount = intent.getLongExtra(PAY_AMOUNT, 0L);
        payMoney.setText("￥" + Utils.getDisplayBalance(lAmount));
        desc.setText(intent.getStringExtra(PAY_DESC));
        confirm.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent result = new Intent();
                result.putExtra(PAY_TYPE, mSelectPayType);
                result.putExtra(PAY_AMOUNT, lAmount);
                PayChooseActivity.this.setResult(Activity.RESULT_OK, result);
                finish();
            }
        });
        payList.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mSelectPayType = position;
                paySelectAdapter.notifyDataSetChanged();
            }
        });
    }

    class PaySelectAdapter extends BaseAdapter {
        private List<PayBean> mPayBeans;

        public PaySelectAdapter(List<PayBean> list) {
            mPayBeans = list;
        }

        @Override
        public int getCount() {
            return mPayBeans != null ? mPayBeans.size() : 0;
        }

        @Override
        public PayBean getItem(int position) {
            return mPayBeans.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View contentView, ViewGroup parent) {
            SimpleViewCache cache = null;
            if (contentView == null) {
                cache = new SimpleViewCache();
                contentView = new SimpleCardListItem(PayChooseActivity.this);
                cache.setBaseView(contentView);
                contentView.setTag(cache);
            } else {
                cache = (SimpleViewCache) contentView.getTag();
                contentView = cache.getBaseView();
            }
            SimpleCardListItem item = (SimpleCardListItem) contentView;
            PayBean payBean = mPayBeans.get(position);
            item.setIcon(payBean.getIcon());
            item.setDescription(payBean.getName());
            item.setSelected(mSelectPayType == payBean.getType() ? true : false);
            return contentView;
        }

    }
}
