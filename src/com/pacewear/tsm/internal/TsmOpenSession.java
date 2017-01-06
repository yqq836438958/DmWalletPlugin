
package com.pacewear.tsm.internal;

import android.text.TextUtils;

import com.pacewear.tsm.TsmConstants;
import com.pacewear.tsm.card.TsmContext;
import com.pacewear.tsm.channel.ITsmAPDUCallback;
import com.pacewear.tsm.common.UniSessionStore;
import com.pacewear.tsm.server.tosservice.CreateSession;
import com.pacewear.tsm.step.IStep;
import com.pacewear.tsm.step.IStep.STATUS;
import com.pacewear.tsm.step.Step;
import com.qq.taf.jce.JceStruct;

import java.util.List;

import TRom.CreateSessionRsp;
import TRom.E_CREATE_SESSION_STEP;

public class TsmOpenSession extends TsmBaseProcess {
    public enum SESSION_STEP {
        ECSS_SELECT, ECSS_INITIALIZE_UPDATE, ECSS_EXTERNAL_AUTHEN, ECSS_FINAL,
    }

    private IStep<SESSION_STEP> mCurStep = null;
    private String mSSDAID = null;

    public TsmOpenSession(TsmContext context, String ssdAID) {
        super(context, TsmConstants.TSM_TYPE_AUTH);
        mSSDAID = ssdAID;
        setSessionStep(mSelectStep, false);
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
            getChannel().selectAID(mSSDAID, new ITsmAPDUCallback() {

                @Override
                public void onSuccess(String[] apdus) {
                    switchStep(mInitUpdateStep);
                }

                @Override
                public void onFail(int error, String desc) {
                    keepStep();
                    postHandleFail(error, desc);
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
            initUpdate.setParams(E_CREATE_SESSION_STEP._ECSS_INITIALIZE_UPDATE, mSSDAID, null,
                    null);
            process(initUpdate, new OnTsmProcessCallback() {

                @Override
                public void onSuccess(String[] apdus) {
                    mExternAuthStep.setAPDU(apdus[0]);
                    switchStep(mExternAuthStep);
                }

                @Override
                public void onFail(int ret, String desc) {
                    keepStep();
                }
            });
        }

    };
    private final SessionStep mExternAuthStep = new SessionStep(SESSION_STEP.ECSS_EXTERNAL_AUTHEN) {

        @Override
        public void onStepHandle() {
            CreateSession externAuth = new CreateSession(mContext);
            externAuth.setParams(E_CREATE_SESSION_STEP._ECSS_EXTERNAL_AUTHEN, mSSDAID, getmAPDU(),
                    getHostChanellege());
            process(externAuth, new OnTsmProcessCallback() {

                @Override
                public void onSuccess(String[] apdus) {
                    switchStep(mFinalStep);
                }

                @Override
                public void onFail(int ret, String desc) {
                    keepStep();
                }
            });
        }

    };
    private final SessionStep mFinalStep = new SessionStep(SESSION_STEP.ECSS_FINAL) {

        @Override
        public void onStepHandle() {
            finishProcess();
        }
    };

    @Override
    protected boolean onStart() {
        return setSessionStep(mCurStep, true);
    }

    @Override
    public boolean isIdle() {
        return (mCurStep == null) || isFinish() || (mCurStep.getStatus() == STATUS.KEEP);
    }

    @Override
    public boolean isFinish() {
        return mFinalStep.isCurrentStep();
    }

    @Override
    protected int onCheck() {
        return CHECK_READY;
    }

    @Override
    protected int getApduList(JceStruct rsp, List<String> apdus, boolean fromLocal) {
        if (fromLocal) {
            return -1;
        }
        CreateSessionRsp data = (CreateSessionRsp) rsp;
        if (data.iRet != 0) {
            return data.iRet;
        }
        String apdu = data.APDU;
        if (TextUtils.isEmpty(apdu)) {
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

}
