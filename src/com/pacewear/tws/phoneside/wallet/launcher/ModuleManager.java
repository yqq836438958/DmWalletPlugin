
package com.pacewear.tws.phoneside.wallet.launcher;

import com.pacewear.tws.phoneside.wallet.bean.ModuleBean;

import java.util.ArrayList;
import java.util.List;

public class ModuleManager {
    private List<ModuleBean> mList = new ArrayList<ModuleBean>();
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
        callback.onCallback(null);
    }
}
