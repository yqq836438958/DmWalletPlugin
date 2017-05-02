
package com.pacewear.tws.phoneside.wallet.ui2.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;

import com.pacewear.tws.phoneside.wallet.R;
import com.pacewear.tws.phoneside.wallet.bean.ModuleBean;
import com.pacewear.tws.phoneside.wallet.launcher.ModuleManager.IMouduleCallback;

import java.util.List;

//plan 2.0
public class MainActivity extends Activity implements IMouduleCallback {
    private BaseAdapter mAdapter = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);
        initViews();
        init();
    }

    private void init() {

    }

    private void initViews() {
        ListView listView = (ListView) findViewById(R.id.module_list);
        
    }

    @Override
    public void onCallback(List<ModuleBean> list) {
        // TODO Auto-generated method stub

    }

    class ModuleListAdapter extends BaseAdapter {
        private List<ModuleBean> moduleBeans = null;

        public ModuleListAdapter(List<ModuleBean> list) {
            moduleBeans = list;
        }

        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return moduleBeans != null ? moduleBeans.size() : 0;
        }

        @Override
        public Object getItem(int position) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // TODO Auto-generated method stub
            return null;
        }

    }
}
