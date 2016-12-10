
package com.pacewear.tws.phoneside.wallet.env;

import TRom.DeviceBaseInfo;
import TRom.PayReqHead;
import TRom.RomAccountInfo;
import TRom.SEBaseInfo;
import TRom.WalletBaseInfo;
import TRom.WatchPayUserAuthInfo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import com.pacewear.tws.phoneside.wallet.common.Constants;
import com.pacewear.tws.phoneside.wallet.common.Utils;
import com.pacewear.tws.phoneside.wallet.step.IStep;
import com.pacewear.tws.phoneside.wallet.step.IStep.COMMON_STEP;
import com.pacewear.tws.phoneside.wallet.step.IStep.STATUS;
import com.pacewear.tws.phoneside.wallet.step.Step;
import com.pacewear.tws.phoneside.wallet.tosservice.ITosService;
import com.pacewear.tws.phoneside.wallet.walletservice.GetCPLC;
import com.pacewear.tws.phoneside.wallet.walletservice.IResult;
import com.tencent.tws.api.BroadcastDef;
import com.tencent.tws.framework.common.DevMgr;
import com.tencent.tws.framework.common.Device;
import com.tencent.tws.framework.global.GlobalObj;
import com.tencent.tws.phoneside.business.AccountManager;
import com.tencent.tws.phoneside.device.wup.DeviceInfoWupDataFactory;
import com.tencent.tws.phoneside.framework.RomBaseInfoHelper;
import com.tencent.tws.proto.wallet.WatchNFCManager.MSG_STATE;

import java.util.ArrayList;
import java.util.Iterator;

import qrom.component.log.QRomLog;

/**
 * @author baodingzhou
 */

public class EnvManager implements IEnvManager, IEnvManagerInner, IEnvManagerListener {

    private static final String TAG = "EnvManager";

    private static volatile IEnvManager sInstance = null;

    private final ArrayList<IEnvManagerListener> mListeners = new ArrayList<IEnvManagerListener>();

    private static final int MSG_WATCH_CONNECTED = 4000;

    private static final int MSG_WATCH_DISCONNECTED = MSG_WATCH_CONNECTED + 1;

    private static final int MSG_WATCH_PAIRED = MSG_WATCH_DISCONNECTED + 1;

    private static final int MSG_WATCH_UNPAIRED = MSG_WATCH_PAIRED + 1;

    private static final int MSG_READ_CPLC_IF_NEED = MSG_WATCH_UNPAIRED + 1;

    private long mLastDeviceDisconnectedMillis = Long.MAX_VALUE;

    private Handler mHandler = null;

    private volatile boolean mWatchConnected = false;

    private IStep<COMMON_STEP> mCurrentCPLCStep = null;

    private volatile String mCPLC = null;

    private String mUserPhoneNum = null;

    private final void onWatchDisconnected() {
        QRomLog.d(TAG, "onWatchDisconnected");
        mWatchConnected = false;
        onWatchConnection(mWatchConnected);
        mLastDeviceDisconnectedMillis = System.currentTimeMillis();
    }

    private final void onWatchConnected() {
        QRomLog.d(TAG, "onWatchConnected");
        mWatchConnected = true;
        onWatchConnection(mWatchConnected);
        if (System.currentTimeMillis() - mLastDeviceDisconnectedMillis > 30000) {
            QRomLog.d(TAG, "Disconnected more than 30s ...");
            syncCPLC(true);
        } else {
            syncCPLC(false);
        }
    }

    private final void onWatchPaired() {
        QRomLog.d(TAG, "onWatchPaired");
        onNewWatchPaired();
    }

    private final void onWatchUnPaired() {
        QRomLog.d(TAG, "onWatchUnPaired");
        onOldWatchUnpaired();
    }

    private final void monitorWathPair() {
        QRomLog.d(TAG, "monitorWathPair");
        IntentFilter intent = new IntentFilter();
        intent.addAction("action_first_connect_by_scan");
        intent.addAction("action_unpair_device");
        GlobalObj.g_appContext.registerReceiver(new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {

                if (intent == null || intent.getAction() == null) {
                    return;
                }

                String action = intent.getAction();
                QRomLog.d(TAG, "monitorWathPair onReceive action:" + action);

                if (action.equalsIgnoreCase("action_first_connect_by_scan")) {
                    mHandler.sendEmptyMessage(MSG_WATCH_PAIRED);
                } else if (action.equalsIgnoreCase("action_unpair_device")) {
                    mHandler.sendEmptyMessage(MSG_WATCH_UNPAIRED);
                }
            }
        }, intent);
    }

    private final void monitorWatchConnection() {
        QRomLog.d(TAG, "monitorWatchConnection");
        IntentFilter intent = new IntentFilter();
        intent.addAction(BroadcastDef.DEVICE_CONNECTED);
        intent.addAction(BroadcastDef.DEVICE_ACTIVE_DISCONNECTED);
        intent.addAction(BroadcastDef.DEVICE_PASSIVE_DISCONNECTED);
        GlobalObj.g_appContext.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                if (intent == null || intent.getAction() == null) {
                    return;
                }

                String action = intent.getAction();
                QRomLog.d(TAG, "monitorWatchConnection onReceive action:" + action);

                if (action.equalsIgnoreCase(BroadcastDef.DEVICE_CONNECTED)) {
                    mHandler.sendEmptyMessage(MSG_WATCH_CONNECTED);
                } else if (action.equalsIgnoreCase(BroadcastDef.DEVICE_ACTIVE_DISCONNECTED)
                        || action.equalsIgnoreCase(BroadcastDef.DEVICE_PASSIVE_DISCONNECTED)) {
                    mHandler.sendEmptyMessage(MSG_WATCH_DISCONNECTED);
                }
            }
        }, intent);
    }

    private EnvManager() {
        QRomLog.d(TAG, "EnvManager()");

        mHandler = new Handler(Utils.getWorkerlooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);

                QRomLog.d(TAG, "handleMessage what: " + msg.what);

                switch (msg.what) {
                    case MSG_WATCH_CONNECTED:
                        onWatchConnected();
                        break;
                    case MSG_WATCH_DISCONNECTED:
                        onWatchDisconnected();
                        break;
                    case MSG_WATCH_PAIRED:
                        onWatchPaired();
                        break;
                    case MSG_WATCH_UNPAIRED:
                        onWatchUnPaired();
                        break;
                    case MSG_READ_CPLC_IF_NEED:
                        break;
                }
            }

        };
        setCPLCStep(mCPLCUnavailable);
        Device device = DevMgr.getInstance().connectedDev();
        if (device != null) {
            mHandler.sendEmptyMessage(MSG_WATCH_CONNECTED);
            mWatchConnected = true;
        } else {
            mHandler.sendEmptyMessage(MSG_WATCH_DISCONNECTED);
            mWatchConnected = false;
        }
        getUserNum();
        monitorWatchConnection();
        monitorWathPair();
    }

    public static IEnvManager getInstance() {
        if (sInstance == null) {
            synchronized (EnvManager.class) {
                if (sInstance == null) {
                    sInstance = new EnvManager();
                }
            }
        }

        return sInstance;
    }

    public static IEnvManagerInner getInstanceInner() {
        return (IEnvManagerInner) getInstance();
    }

    @Override
    public boolean registerEnvManagerListener(IEnvManagerListener listener) {
        QRomLog.d(TAG, "registerEnvManagerListener");
        if (listener == null) {
            return false;
        }
        synchronized (mListeners) {
            if (!mListeners.contains(listener)) {
                return mListeners.add(listener);
            }
        }
        return false;
    }

    @Override
    public boolean unregisterEnvManagerListener(IEnvManagerListener listener) {
        QRomLog.d(TAG, "unregisterEnvManagerListener");
        if (listener == null) {
            return false;
        }
        synchronized (mListeners) {
            return mListeners.remove(listener);
        }
    }

    @Override
    public boolean onNewWatchPaired() {
        QRomLog.d(TAG, "onNewWatchPaired");

        Iterator<IEnvManagerListener> iterator = null;
        IEnvManagerListener listener = null;

        synchronized (mListeners) {
            iterator = mListeners.iterator();
            if (iterator != null) {
                while (iterator.hasNext()) {
                    listener = iterator.next();
                    listener.onNewWatchPaired();
                }
            }
        }
        getUserNum();
        return true;
    }

    @Override
    public boolean onWatchIdentified(boolean succeed, String cplc) {
        QRomLog.d(TAG, String.format("onWatchIdentified succeed:%b cplc:%s", succeed, cplc));

        Iterator<IEnvManagerListener> iterator = null;
        IEnvManagerListener listener = null;

        synchronized (mListeners) {
            iterator = mListeners.iterator();
            if (iterator != null) {
                while (iterator.hasNext()) {
                    listener = iterator.next();
                    listener.onWatchIdentified(succeed, cplc);
                }
            }
        }

        return true;
    }

    @Override
    public boolean onOldWatchUnpaired() {
        QRomLog.d(TAG, "onOldWatchUnpaired");

        Iterator<IEnvManagerListener> iterator = null;
        IEnvManagerListener listener = null;

        synchronized (mListeners) {
            iterator = mListeners.iterator();
            if (iterator != null) {
                while (iterator.hasNext()) {
                    listener = iterator.next();
                    listener.onOldWatchUnpaired();
                }
            }
        }

        return true;
    }

    @Override
    public boolean onWatchConnection(boolean connected) {
        QRomLog.d(TAG, "onWatchConnection connected:" + connected);

        Iterator<IEnvManagerListener> iterator = null;
        IEnvManagerListener listener = null;

        synchronized (mListeners) {
            iterator = mListeners.iterator();
            if (iterator != null) {
                while (iterator.hasNext()) {
                    listener = iterator.next();
                    listener.onWatchConnection(connected);
                }
            }
        }
        return true;
    }

    private void syncCPLC(boolean force) {
        QRomLog.d(TAG, "syncCPLC force:" + force);
        if (!mCPLCReady.isCurrentStep()) {
            mCurrentCPLCStep.onStep();
        } else if (force) {
            setCPLCStep(mCPLCDubious);
        }
    }

    @Override
    public boolean forceSyncCPLC(final boolean isForce) {
        return mHandler.post(new Runnable() {
            @Override
            public void run() {
                syncCPLC(isForce);
            }
        });
    }

    private abstract class CPLCStep extends Step<COMMON_STEP> {

        private long mUniqueSeq = -1;

        public CPLCStep(COMMON_STEP step) {
            super(step);

            // Initialized status
            if (step == COMMON_STEP.UNAVAILABLE) {
                mStatus = STATUS.KEEP;
            }
        }

        @Override
        protected boolean setStep(IStep<COMMON_STEP> step) {
            return EnvManager.this.setCPLCStep(step);
        }

        @Override
        protected void notifyStepStatus(COMMON_STEP step, STATUS status) {
            QRomLog.d(TAG,
                    String.format("CPLCStep.notifyStepStatus step:%s status:%s", step, status));
            switch (status) {
                case KEEP:
                    // sync cplc error
                    onWatchIdentified(false, null);
                    break;
                case HANDLE:
                    switch (step) {
                        case UPDATED:
                            // THE CPLC changed.
                            onNewWatchPaired();
                            break;
                        case READY:
                            onWatchIdentified(true, mCPLC);
                            break;
                        default:
                            break;
                    }
                    break;
                default:
                    break;
            }
        }

        protected void syncCPLC() {
            QRomLog.d(TAG, "syncCPLC");
            GetCPLC getCPLC = new GetCPLC();
            mUniqueSeq = getCPLC.getSeqID();
            boolean handled = getCPLC.invoke(new IResult() {

                @Override
                public void onResult(long seqID, int ret, String[] outputParams,
                        Integer[] resultCode, byte[] bytes) {
                    if (mUniqueSeq == seqID) {
                        String cplc = (outputParams != null && outputParams.length > 0)
                                ? outputParams[0] : null;
                        QRomLog.d(TAG, String.format("syncCPLC.onResult seqID:%d ret:%d cplc:%s",
                                seqID, ret, cplc));
                        if (ret == 0 && cplc != null) {
                            if (mCPLC == null) {
                                mCPLC = cplc;
                                switchStep(mCPLCReady);
                            } else if (!mCPLC.equalsIgnoreCase(cplc)) {
                                mCPLC = cplc;
                                switchStep(mCPLCUpdated);
                            } else {
                                switchStep(mCPLCReady);
                            }
                        } else {
                            keepStep();
                        }
                    }
                }

                @Override
                public void onExecption(long seqID, int error) {
                    if (mUniqueSeq == seqID) {
                        QRomLog.d(TAG, String.format("syncCPLC.onExecption seqID:%d error:%s",
                                seqID, MSG_STATE.convert(error)));
                        keepStep();
                    }
                }
            });

            if (!handled) {
                keepStep();
            }
        }
    }

    private final boolean setCPLCStep(IStep<COMMON_STEP> step) {

        boolean handle = false;

        if (step == null) {
            return handle;
        }

        if (mCurrentCPLCStep != step) {
            IStep<COMMON_STEP> previousStep = mCurrentCPLCStep;
            mCurrentCPLCStep = step;
            if (previousStep != null) {
                previousStep.onQuitStep();
            }
            mCurrentCPLCStep.onEnterStep();
            handle = true;
        }

        return handle;
    }

    private final CPLCStep mCPLCUnavailable = new CPLCStep(COMMON_STEP.UNAVAILABLE) {
        @Override
        public void onStepHandle() {
            syncCPLC();
        }
    };

    private final CPLCStep mCPLCUpdated = new CPLCStep(COMMON_STEP.UPDATED) {
        @Override
        public void onStepHandle() {
            switchStep(mCPLCReady);
        }
    };

    private final CPLCStep mCPLCReady = new CPLCStep(COMMON_STEP.READY) {
        @Override
        public void onStepHandle() {
            // Do nothing.
        }
    };

    private final CPLCStep mCPLCDubious = new CPLCStep(COMMON_STEP.DUBIOUS) {
        @Override
        public void onStepHandle() {
            syncCPLC();
        }
    };

    @Override
    public boolean isCPLCReady() {
        return mCPLCReady.isCurrentStep();
    }

    @Override
    public final boolean isWatchConnected() {
        return mWatchConnected;
    }

    @Override
    public final WatchPayUserAuthInfo getUserAuthInfo() {
        // TODO The nick name
        RomAccountInfo accountInfo = AccountManager.getInstance().getLoginAccountIdInfo();
        return new WatchPayUserAuthInfo(accountInfo, "test");
    }

    @Override
    public final DeviceBaseInfo getDeviceBaseInfo() {
        DeviceBaseInfo tmp = new DeviceBaseInfo();
        tmp.stPhoneBaseInfo = RomBaseInfoHelper.getRomBaseInfo();
        tmp.stWatchBaseInfo = DeviceInfoWupDataFactory.getInstance().getWatchRomBaseInfo();
        return tmp;
    }

    @Override
    public final SEBaseInfo getSeBaseInfo() {
        if (isCPLCReady()) {
            return new SEBaseInfo(mCPLC);
        } else {
            QRomLog.d(TAG, "getSeBaseInfo error");
        }

        return null;
    }

    @Override
    public final PayReqHead getPayReqHead() {

        SEBaseInfo seBaseInfo = getSeBaseInfo();
        if (seBaseInfo != null) {
            return new PayReqHead(seBaseInfo, getDeviceBaseInfo(), getUserAuthInfo(),
                    ITosService.MODULE_VER);
        } else {
            QRomLog.d(TAG, "getPayReqHead error");
        }

        return null;
    }

    @Override
    public final WalletBaseInfo getWalletBaseInfo() {
        return new WalletBaseInfo("Tencent", "Taishan001", Constants.WALLET_PACKAGE_NAME);
    }

    @Override
    public String getUserPhoneNum() {
        return mUserPhoneNum;
    }

    @Override
    public void setUserPhoneNum(String phone) {
        mUserPhoneNum = phone;
    }

    private void getUserNum() {
        if (!TextUtils.isEmpty(mUserPhoneNum)) {
            return;
        }
        String tmpPhone = null;// SmsModel.get().getPhoneNum();
        if (!TextUtils.isEmpty(tmpPhone)) {
            setUserPhoneNum(tmpPhone);
            return;
        }
        Utils.getWorkerHandler().post(new Runnable() {

            @Override
            public void run() {
                // SmsModel.get().getPhoneNum(new OnSmsCallback() {
                // @Override
                // public void OnResult(int ret, String result) {
                // if (ret == 0 && !TextUtils.isEmpty(result)) {
                // setUserPhoneNum(result);
                // }
                // }
                // });
            }
        });
    }
}
