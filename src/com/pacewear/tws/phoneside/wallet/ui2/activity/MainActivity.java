
package com.pacewear.tws.phoneside.wallet.ui2.activity;

import android.app.TwsActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;

import com.pacewear.tws.phoneside.wallet.R;
import com.pacewear.tws.phoneside.wallet.WalletApp;
import com.pacewear.tws.phoneside.wallet.bean.ModuleBean;
import com.pacewear.tws.phoneside.wallet.launcher.ModuleManager;
import com.pacewear.tws.phoneside.wallet.launcher.ModuleManager.IMouduleCallback;
import com.pacewear.tws.phoneside.wallet.ui.widget.SimpleCardListItem;
import com.pacewear.tws.phoneside.wallet.ui2.widget.SimpleViewCache;
import com.tencent.tws.assistant.app.ActionBar;
import com.tencent.tws.assistant.widget.Toast;

import java.util.List;

//plan 2.0
public class MainActivity extends TwsActivity implements IMouduleCallback {
    private ModuleListAdapter mAdapter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);
        initViews();
        init();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void init() {
        ModuleManager.getInstance().reqListAsync(this);
    }

    private void initViews() {
        ActionBar actionBar = getTwsActionBar();
        actionBar.setTitle(R.string.nfc_wallet);
        ListView listView = (ListView) findViewById(R.id.module_list);
        mAdapter = new ModuleListAdapter(null);
        listView.setAdapter(mAdapter);
    }

    @Override
    public void onCallback(List<ModuleBean> list) {
        mAdapter.refresh(list);
    }

    class ModuleListAdapter extends BaseAdapter {
        private List<ModuleBean> moduleBeans = null;

        public ModuleListAdapter(List<ModuleBean> list) {
            moduleBeans = list;
        }

        public void refresh(List<ModuleBean> list) {
            moduleBeans = list;
            this.notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return moduleBeans != null ? moduleBeans.size() : 0;
        }

        @Override
        public ModuleBean getItem(int position) {
            return moduleBeans.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            SimpleViewCache cache = null;
            if (convertView == null) {
                cache = new SimpleViewCache();
                convertView = new SimpleCardListItem(MainActivity.this);
                cache.setBaseView(convertView);
                convertView.setTag(cache);
            } else {
                cache = (SimpleViewCache) convertView.getTag();
                convertView = cache.getBaseView();
            }
            SimpleCardListItem item = (SimpleCardListItem) convertView;
            ModuleBean bean = moduleBeans.get(position);
            item.setIcon(bean.getIcon());
            item.setDescription(getString(bean.getName()));
            item.setRightBitmap(R.drawable.arrow);
            final String targetClass = bean.getTargetClass();
            convertView.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (!goTargetActivity(targetClass)) {
                        Toast.makeText(WalletApp.getHostAppContext(), "Not Support",
                                Toast.LENGTH_LONG).show();
                    }
                }
            });
            return convertView;
        }

    }

    private boolean goTargetActivity(String className) {
        try {
            Intent intent = new Intent();
            Class<?> target = Class.forName(className);
            intent.setClass(MainActivity.this, target);
            startActivity(intent);
            return true;
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return false;
    }
}
