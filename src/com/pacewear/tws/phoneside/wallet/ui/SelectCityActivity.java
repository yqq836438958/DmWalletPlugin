
package com.pacewear.tws.phoneside.wallet.ui;

import android.app.TwsActivity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;

import com.tencent.tws.assistant.app.ActionBar;
import com.tencent.tws.assistant.widget.AdapterView;
import com.tencent.tws.assistant.widget.AdapterView.OnItemClickListener;
import com.tencent.tws.assistant.widget.ListView;
import com.tencent.tws.gdevicemanager.R;
import com.tencent.tws.phoneside.walletv2.common.Utils;
import com.tencent.tws.phoneside.walletv2.ui.widget.SimpleCardListItem;
import com.tencent.tws.phoneside.walletv2.ui.widget.SimpleCardListItem.SimpleViewCache;

import java.util.ArrayList;
import java.util.List;

public class SelectCityActivity extends TwsActivity {
    public static final String CARD_NAME = "cardname";
    public static final String SELECT_CITY_NAME = "select_city_name";
    public static final String DEFAULT_CITY_NAME = "default_city_name";
    public static final String SELECT_CITY_CODE = "select_city_code";
    public static final String DEFAULT_CITY_CODE = "default_city_code";
    private ArrayList<String> mCityNameList = new ArrayList<String>();
    private ArrayList<String> mCityCodeList = new ArrayList<String>();
    private Context mContext = null;
    private String mCardName = "";
    private String mSelectCityName = "";
    private String mDefaultCityName = "";
    private String mSelectCityCode = "";
    private String mDefaultCityCode = "";
    private static final int DEFALUT_POS = -99;
    private int mSelectCityPos = -1;
    private SimpleCardListItem mDefaultCityLayout = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wallet_select_city);
        mContext = this;
        loadCitys();
        initViews();
    }

    private void initViews() {
        ActionBar actionBar = getTwsActionBar();
        actionBar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(
                R.color.wallet_action_bar_background)));
        actionBar.setStackedBackgroundDrawable(
                getResources().getDrawable(R.drawable.ab_solid_light_holo_opacity));

        mDefaultCityLayout = (SimpleCardListItem) findViewById(
                R.id.lay_default_city);
        mDefaultCityLayout.setIcon(R.drawable.wallet_ic_postion);
        mDefaultCityLayout.setDescription(mDefaultCityName);
        mDefaultCityLayout.setItemSelect(mDefaultCityName.equalsIgnoreCase(mSelectCityName));
        mDefaultCityLayout.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                mSelectCityPos = DEFALUT_POS;
                mDefaultCityLayout.setItemSelect(true);
                mSelectCityName = mDefaultCityName;
                mSelectCityCode = mDefaultCityCode;
                mCityListAdapter.notifyDataSetChanged();
            }
        });
        ListView cityListView = (ListView) findViewById(R.id.lv_citylist);
        cityListView.setAdapter(mCityListAdapter);
        cityListView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int postion, long arg3) {
                mSelectCityPos = postion;
                mCityListAdapter.notifyDataSetChanged();
                mDefaultCityLayout.setItemSelect(false);
            }
        });

    }

    private void loadCitys() {
        Intent intent = getIntent();
        mCardName = intent.getStringExtra(CARD_NAME);
        mSelectCityName = intent.getStringExtra(SELECT_CITY_NAME);
        mSelectCityCode = intent.getStringExtra(SELECT_CITY_CODE);
        mDefaultCityName = intent.getStringExtra(DEFAULT_CITY_NAME);
        mDefaultCityCode = intent.getStringExtra(DEFAULT_CITY_CODE);
        if (mSelectCityCode.equals(mDefaultCityCode)) {
            mSelectCityPos = DEFALUT_POS;
        }
        List<String> list = Utils.getCityList();
        String[] tmp = null;
        for (String str : list) {
            tmp = str.split("#");
            if (tmp != null && tmp.length >= 3) {
                if (!TextUtils.isEmpty(tmp[2]) && !tmp[1].equals(mDefaultCityCode)) {
                    mCityCodeList.add(tmp[1]);
                    mCityNameList.add(tmp[2]);
                }
            }
        }
        if (mCityCodeList == null) {
            return;
        }
        for (int i = 0; i < mCityCodeList.size(); i++) {
            if (mCityCodeList.get(i).equalsIgnoreCase(mSelectCityCode)
                    && mSelectCityPos != DEFALUT_POS) {
                mSelectCityPos = i;
                break;
            }
        }
    }

    private BaseAdapter mCityListAdapter = new BaseAdapter() {
        @Override
        public int getCount() {
            return (mCityCodeList == null) ? 0 : mCityCodeList.size();
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
            SimpleViewCache cache = null;
            if (contentView == null) {
                cache = new SimpleViewCache();
                contentView = new SimpleCardListItem(mContext);
                cache.setBaseView(contentView);
                contentView.setTag(cache);
            } else {
                cache = (SimpleViewCache) contentView.getTag();
                contentView = cache.getBaseView();
            }
            SimpleCardListItem item = (SimpleCardListItem) contentView;
            String citycode = mCityCodeList.get(postion);
            String cityname = mCityNameList.get(postion);
            String displayname = (TextUtils.isEmpty(cityname)) ? mCardName
                    : new StringBuilder(mCardName).append("·").append(cityname).toString();
            item.setDescription(displayname);
            item.setIcon(0);
            if (mSelectCityPos == postion) {
                item.setItemSelect(true);
                mSelectCityCode = citycode;
                mSelectCityName = displayname;
            } else {
                item.setItemSelect(false);
            }
            return contentView;
        }
    };

    @Override
    public void onBackPressed() {
        onBackEvent();
        super.onBackPressed();
    }

    private void onBackEvent() {
        Intent intent = new Intent();
        intent.putExtra(SELECT_CITY_NAME, mSelectCityName);
        intent.putExtra(SELECT_CITY_CODE, mSelectCityCode);
        setResult(RESULT_OK, intent);
    }
}
