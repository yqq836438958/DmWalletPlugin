
package com.pacewear.tws.phoneside.wallet.ui.handler;

import com.pacewear.tws.phoneside.wallet.order.IOrder;
import com.pacewear.tws.phoneside.wallet.order.OrderManager;
import com.pacewear.tws.phoneside.wallet.step.IStep.COMMON_STEP;
import com.pacewear.tws.phoneside.wallet.step.IStep.STATUS;

import java.util.Arrays;
import java.util.List;

public class WalletHandler_CardInfo extends WalletBaseHandler {
    public WalletHandler_CardInfo() {
        super();
        mCurModule = MODULE_CALLBACK.MODULE_CARD;
        mWhiteListScenes = new ACTVITY_SCENE[] {
                ACTVITY_SCENE.SCENE_SWITCHCARD,
                ACTVITY_SCENE.SCENE_SYNCALL, ACTVITY_SCENE.SCENE_ISSE_TOPUP
        };
    }

    private String mAID = "";

    public void exec(String aid, COMMON_STEP step, STATUS status) {
        mAID = aid;
        super.exec(step, status);
    }

    @Override
    protected int onPostHandle() {
        if (mCurSence == ACTVITY_SCENE.SCENE_SYNCALL) {
            mForceUpdateUI = (mEventResult != 0);
            return WALLET_HANDLED;
        }
        if (!mSceneAID.equals(mAID)) {
            return WALLET_HANDLE_IGNORE;
        }
        if (isOrderBusinessWorking()) {
            return WALLET_HANDLE_IGNORE;
        }
        return WALLET_HANDLED;
    }

    private boolean isOrderBusinessWorking() {
        if (mCurSence != ACTVITY_SCENE.SCENE_ISSE_TOPUP) {
            return false;
        }
        IOrder order = OrderManager.getInstance().getLastOrder(mAID);
        if (order == null) {
            return false;
        }
        return !order.isIdle();
    }
}
