
package com.pacewear.tws.phoneside.wallet.ui.handler;

import com.pacewear.tws.phoneside.wallet.order.IOrderManagerListener.MODULE;
import com.pacewear.tws.phoneside.wallet.step.IStep.COMMON_STEP;
import com.pacewear.tws.phoneside.wallet.step.IStep.STATUS;

public class WalletHandler_OrderManager extends WalletBaseHandler {
    public WalletHandler_OrderManager() {
        super();
        mCurModule = MODULE_CALLBACK.MODULE_ORDER;
        mWhiteListScenes = new ACTVITY_SCENE[] {
                ACTVITY_SCENE.SCENE_SYNCALL
        };
    }

    public void exec(MODULE module, COMMON_STEP step, STATUS status) {
        if (module != MODULE.ORDER_LIST) {
            return;
        }
        super.exec(step, status);
    }

    @Override
    protected int onPostHandle() {
        if (mEventResult == 0) {
            mForceUpdateUI = false;
        }
        return WALLET_HANDLED;
    }
}
