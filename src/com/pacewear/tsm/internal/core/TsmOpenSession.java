
package com.pacewear.tsm.internal.core;

import android.text.TextUtils;
import android.util.Log;

import com.pacewear.tsm.card.TsmCard;
import com.pacewear.tsm.card.TsmContext;
import com.pacewear.tsm.common.UniSessionStore;
import com.pacewear.tsm.server.tosservice.CreateSession;
import com.pacewear.tsm.step.IStep;
import com.pacewear.tsm.step.Step;
import com.qq.taf.jce.JceStruct;

import java.util.List;

import TRom.CreateSessionRsp;
import TRom.E_CREATE_SESSION_STEP;

public class TsmOpenSession implements IProcessEventConsum {
    public enum SESSION_STEP {
        ECSS_SELECT, ECSS_INITIALIZE_UPDATE, ECSS_EXTERNAL_AUTHEN, ECSS_FINAL,
    }
    public static final String TAG = "TSM";
    private IStep<SESSION_STEP> mCurStep = null;
    private boolean mNeedSession = false;
    private IApduTransmiter mApduTransmiter = null;
    private TsmContext mContext = null;
    private String mSessionAID = null;
    private OnTsmProcessCallback mOutProcessCallback = null;

    public TsmOpenSession(TsmContext context, String ssdAID, boolean needAuth) {
        mContext = context;
        mSessionAID = ssdAID;
        mNeedSession = needAuth;
        mApduTransmiter = new ApduTransmiter(context.getChannel(), this);
    }

    public boolean invoke(OnTsmProcessCallback callback) {
        mOutProcessCallback = callback;
        return setSessionStep(mSelectStep, true);
    }

    private boolean setSessionStep(IStep<SESSION_STEP> step, boolean execNow) {
        if (step == null) {
            return false;
        }

        if (mCurStep != step) {
            IStep<SESSION_STEP> previousStep = mCurStep;
            mCurStep = step;
            if (previousStep != null) {
                previousStep.onQuitStep();
            }
        }
        if (execNow) {
            mCurStep.onEnterStep();
        }
        return true;
    }

    private abstract class SessionStep extends Step<SESSION_STEP> {
        private String mAPDU = null;
        private String mHostChanellege = null;

        public SessionStep(SESSION_STEP step) {
            super(step);
        }

        public void setAPDU(String apdu) {
            mAPDU = apdu;
        }

        public String getmAPDU() {
            return mAPDU;
        }

        public void setHostChanellege(String mHostChanellege) {
            this.mHostChanellege = mHostChanellege;
        }

        public String getHostChanellege() {
            return mHostChanellege;
        }

        @Override
        protected boolean setStep(IStep<SESSION_STEP> step) {
            return setSessionStep(step, true);
        }

        @Override
        protected void notifyStepStatus(SESSION_STEP step,
                STATUS status) {

        }
    }

    private final SessionStep mSelectStep = new SessionStep(SESSION_STEP.ECSS_SELECT) {

        @Override
        public void onStepHandle() {
            mApduTransmiter.selectAID(mSessionAID, new OnTsmProcessCallback() {

                @Override
                public void onSuccess(String[] apduList) {
                    TsmCard card = mContext.getCard();
                    card.setFocusAID(mSessionAID);
                    if (mNeedSession) {
                        switchStep(mInitUpdateStep);
                    } else {
                        switchStep(mFinalStep);
                    }
                }

                @Override
                public void onFail(int error, String desc) {
                    Log.e(TAG, "TsmOpenSession: select " + mSessionAID + " failed,error:" + error
                            + ",desc:" + desc);
                    keepStep();
                    if (mOutProcessCallback != null) {
                        mOutProcessCallback.onFail(error, desc);
                    }
                }
            });
        }
    };
    private final SessionStep mInitUpdateStep = new SessionStep(
            SESSION_STEP.ECSS_INITIALIZE_UPDATE) {

        @Override
        public void onStepHandle() {
            UniSessionStore.getInstance().clear();
            CreateSession initUpdate = new CreateSession(mContext);
            initUpdate.setParams(E_CREATE_SESSION_STEP._ECSS_INITIALIZE_UPDATE, mSessionAID, null,
                    null);
            mApduTransmiter.transmit(initUpdate, new OnTsmProcessCallback() {

                @Override
                public void onSuccess(String[] apdus) {
                    mExternAuthStep.setAPDU(apdus[0]);
                    switchStep(mExternAuthStep);
                }

                @Override
                public void onFail(int ret, String desc) {
                    Log.e(TAG, "TsmOpenSession: i-u failed,error:" + ret
                            + ",desc:" + desc);
                    keepStep();
                    if (mOutProcessCallback != null) {
                        mOutProcessCallback.onFail(ret, desc);
                    }
                }
            });
        }

    };
    private final SessionStep mExternAuthStep = new SessionStep(SESSION_STEP.ECSS_EXTERNAL_AUTHEN) {

        @Override
        public void onStepHandle() {
            CreateSession externAuth = new CreateSession(mContext);
            externAuth.setParams(E_CREATE_SESSION_STEP._ECSS_EXTERNAL_AUTHEN, mSessionAID,
                    getmAPDU(), getHostChanellege());
            mApduTransmiter.transmit(externAuth, new OnTsmProcessCallback() {

                @Override
                public void onSuccess(String[] apdus) {
                    switchStep(mFinalStep);
                }

                @Override
                public void onFail(int ret, String desc) {
                    Log.e(TAG, "TsmOpenSession: e-a failed,error:" + ret
                            + ",desc:" + desc);
                    keepStep();
                    if (mOutProcessCallback != null) {
                        mOutProcessCallback.onFail(ret, desc);
                    }
                }
            });
        }

    };
    private final SessionStep mFinalStep = new SessionStep(SESSION_STEP.ECSS_FINAL) {

        @Override
        public void onStepHandle() {
            if (mOutProcessCallback != null) {
                mOutProcessCallback.onSuccess(null);
            }
        }
    };

    @Override
    public int onParserApdu(JceStruct rsp, List<String> apdus, boolean fromLocal) {
        if (fromLocal) {
            return -1;
        }
        CreateSessionRsp data = (CreateSessionRsp) rsp;
        if (data.iRet != 0) {
            Log.e(TAG, "TsmOpenSession: onParserApdu failed,error:" + data.iRet);
            return data.iRet;
        }
        String apdu = data.APDU;
        if (TextUtils.isEmpty(apdu)) {
            Log.e(TAG, "TsmOpenSession: onParserApdu failed,apdu null");
            return -1;
        }
        apdus.add(apdu);
        String hostChanllege = data.sHostChallenge;
        if (mInitUpdateStep.isCurrentStep()) {
            UniSessionStore.getInstance().update(data.sSessionId);
        } else if (mExternAuthStep.isCurrentStep()) {
            mExternAuthStep.setHostChanellege(hostChanllege);
        }
        return data.iRet;
    }

    @Override
    public boolean returnWithoutTransmit() {
        // TODO Auto-generated method stub
        return false;
    }

}
