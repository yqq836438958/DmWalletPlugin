
package com.pacewear.tws.phoneside.wallet.ui.handler;

import com.pacewear.tws.phoneside.wallet.step.IStep.COMMON_STEP;
import com.pacewear.tws.phoneside.wallet.step.IStep.STATUS;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public abstract class WalletBaseHandler {
    public static enum ACTVITY_SCENE {
        SCENE_UNKOWN, SCENE_SYNCALL, SCENE_SWITCHCARD, SCENE_ISSE_TOPUP
    }

    public static enum MODULE_CALLBACK {
        MODULE_CARD, MODULE_ORDER,
    }

    public static final int WALLET_HANDLED = 0;
    public static final int WALLET_HANDLE_IGNORE = 1;
    public static final int WALLET_HANDLE_EXCEPTION = -1;
    public static ACTVITY_SCENE mCurSence = ACTVITY_SCENE.SCENE_UNKOWN;
    protected int mEventResult = 0;
    protected boolean mForceUpdateUI = true;
    private HashMap<ACTVITY_SCENE, OnWalletUICallback> mUiCallbackMaps = new HashMap<ACTVITY_SCENE, OnWalletUICallback>();
    protected String mSceneAID = "";
    protected ACTVITY_SCENE[] mWhiteListScenes;
    protected MODULE_CALLBACK mCurModule;
    private static Object sLockObj = new Object();

    public static interface OnWalletUICallback {
        public void onUIUpdate(MODULE_CALLBACK module, int ret, boolean forUpdateUI);
    }

    public WalletBaseHandler() {
    }

    public static void setCurScene(ACTVITY_SCENE scene) {
        mCurSence = scene;
    }

    protected boolean handleByChild = false;

    public void exec(COMMON_STEP step, STATUS status) {
        if (!this.handleByChild) {
            boolean handle = false;
            switch (status) {
                case HANDLE:
                    if (step == COMMON_STEP.READY) {
                        mEventResult = 0;
                        handle = true;
                    }
                    break;
                case KEEP:
                    mEventResult = -1;
                    handle = true;
                    break;
                default:
                    break;
            }
            if (!handle) {
                return;
            }
        }
        handleScene();
    }

    public final void registCallback(String aid, ACTVITY_SCENE scene, OnWalletUICallback callback) {
        if (!isInWhiteList(scene)) {
            return;
        }
        synchronized (sLockObj) {
            mSceneAID = (aid == null) ? "" : aid;
            if (!mUiCallbackMaps.containsValue(callback)) {
                mUiCallbackMaps.put(scene, callback);
            }
        }
    }

    public final void unregistCallback(ACTVITY_SCENE scene) {
        synchronized (sLockObj) {
            mUiCallbackMaps.remove(scene);
        }
    }

    private void handleScene() {
        if (!isInWhiteList(mCurSence)) {
            return;
        }
        int handle = onPostHandle();
        if (handle == WALLET_HANDLED) {
            postResultToUI();
        } else if (handle == WALLET_HANDLE_EXCEPTION) {
            mEventResult = -1;
            mForceUpdateUI = true;
            postResultToUI();
        }
    }

    private void postResultToUI() {
        synchronized (sLockObj) {
            Iterator iter = mUiCallbackMaps.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry entry = (Map.Entry) iter.next();
                ACTVITY_SCENE scene = (ACTVITY_SCENE) entry.getKey();
                OnWalletUICallback callback = (OnWalletUICallback) entry.getValue();
                if (scene == mCurSence) {
                    if (callback != null) {
                        callback.onUIUpdate(mCurModule, mEventResult, mForceUpdateUI);
                    }
                    break;
                }
            }
        }
    }

    private boolean isInWhiteList(ACTVITY_SCENE scene) {
        if (scene == ACTVITY_SCENE.SCENE_UNKOWN) {
            return false;
        }
        List<ACTVITY_SCENE> whiteList = Arrays.asList(mWhiteListScenes);
        if (whiteList == null) {
            return false;
        }
        return whiteList.contains(scene);
    }

    protected abstract int onPostHandle();
}
