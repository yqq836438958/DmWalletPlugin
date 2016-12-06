
package com.pacewear.tws.phoneside.wallet.ui.handler;

public class WalletHandler_CardList extends WalletBaseHandler {
    public WalletHandler_CardList() {
        super();
        mCurModule = MODULE_CALLBACK.MODULE_CARD;
        mWhiteListScenes = new ACTVITY_SCENE[] {
                ACTVITY_SCENE.SCENE_SWITCHCARD,
                ACTVITY_SCENE.SCENE_SYNCALL, ACTVITY_SCENE.SCENE_ISSE_TOPUP
        };
    }

    @Override
    protected int onPostHandle() {
        if (mCurSence == ACTVITY_SCENE.SCENE_SYNCALL) {
            mForceUpdateUI = (mEventResult != 0);
            return WALLET_HANDLED;
        }
        if (mEventResult != 0) {
            return WALLET_HANDLED;
        }
        if (mCurSence == ACTVITY_SCENE.SCENE_ISSE_TOPUP) {
            return WALLET_HANDLE_IGNORE;
        }
        return WALLET_HANDLED;
    }

}
