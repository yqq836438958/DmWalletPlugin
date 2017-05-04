
package com.pacewear.tws.phoneside.wallet.launcher;

import com.pacewear.tws.phoneside.wallet.R;
import com.pacewear.tws.phoneside.wallet.bean.ModuleBean;
import com.pacewear.tws.phoneside.wallet.order.IOrderManager.ORDER_STEP;
import com.pacewear.tws.phoneside.wallet.order.IOrderManagerListener;
import com.pacewear.tws.phoneside.wallet.step.IStep.COMMON_STEP;
import com.pacewear.tws.phoneside.wallet.step.IStep.STATUS;

import java.util.ArrayList;
import java.util.List;

public class ModuleManager {
    private static ModuleManager sInstance = null;

    public static interface IMouduleCallback {
        public void onCallback(List<ModuleBean> list);
    }

    public static ModuleManager getInstance() {
        if (sInstance == null) {
            synchronized (ModuleManager.class) {
                sInstance = new ModuleManager();
            }
        }
        return sInstance;
    }

    public void reqListAsync(IMouduleCallback callback) {
        List<ModuleBean> list = new ArrayList<ModuleBean>();
        list.add(new ModuleBean(R.drawable.ic_transportation_card,
                R.string.wallet_launcher_trafficcard,
                "com.pacewear.tws.phoneside.wallet.ui2.activity.TrafficCardActivity"));
        // TODO ADD OTHER LAUNCHER
        list.add(new ModuleBean(R.drawable.ic_bank_card, R.string.wallet_launcher_bankcard, ""));
        list.add(
                new ModuleBean(R.drawable.ic_campus_card, R.string.wallet_launcher_schoolcard, ""));
        callback.onCallback(list);
    }

}
