
package com.pacewear.tws.phoneside.wallet.ui.handler;

public class WalletHandler_NewOrder extends WalletBaseHandler {
    public WalletHandler_NewOrder() {
        super();
        mCurModule = MODULE_CALLBACK.MODULE_ORDER;
        mWhiteListScenes = new ACTVITY_SCENE[] {
                ACTVITY_SCENE.SCENE_ISSE_TOPUP
        };
    }

    public void exec(long uniqueSeq, boolean succeed, String tradeNo) {
        if (tradeNo == null) {
            mEventResult = -1;
            handleByChild = true;
            super.exec(null, null);
        }
    }

    @Override
    protected int onPostHandle() {
        return WALLET_HANDLED;
    }

}
