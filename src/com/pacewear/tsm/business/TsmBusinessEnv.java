
package com.pacewear.tsm.business;

import android.text.TextUtils;

import com.pacewear.httpserver.IResponseObserver;
import com.pacewear.tsm.card.TsmCard;
import com.pacewear.tsm.card.TsmContext;
import com.pacewear.tsm.channel.ITsmAPDUCallback;
import com.pacewear.tsm.channel.ITsmCardChannel;
import com.pacewear.tsm.common.CacheUtil;
import com.pacewear.tsm.server.tosservice.SyncRemoteCardStatus;
import com.pacewear.tsm.step.IStep;
import com.qq.taf.jce.JceStruct;

import TRom.CardStatusContextRsp;

import com.pacewear.tsm.step.Step;

public class TsmBusinessEnv {
    public enum ENV_STEP {
        CHECK, GET_CPLC, SYNC_SERVER, FINAL
    }

    public static interface OnBusinessEnvCallback {

        public void onSucess();

        public void onFail(int ret, String desc);
    }

    private TsmContext mContext = null;
    private IStep<ENV_STEP> mCurEnvStep = null;
    private OnBusinessEnvCallback mCallback = null;

    public TsmBusinessEnv(TsmContext context) {
        mContext = context;
    }

    public boolean setup(OnBusinessEnvCallback callback) {
        mCallback = callback;
        return setEnvStep(mCheckEnvStep, true);
    }

    private abstract class EnvStep extends Step<ENV_STEP> {

        public EnvStep(ENV_STEP step) {
            super(step);
        }

        @Override
        protected boolean setStep(IStep<ENV_STEP> step) {
            return setEnvStep(step, true);
        }

        @Override
        protected void notifyStepStatus(ENV_STEP step,
                STATUS status) {

        }
    }

    private boolean setEnvStep(IStep<ENV_STEP> step, boolean execNow) {
        if (step == null) {
            return false;
        }

        if (mCurEnvStep != step) {
            IStep<ENV_STEP> previousStep = mCurEnvStep;
            mCurEnvStep = step;
            if (previousStep != null) {
                previousStep.onQuitStep();
            }
        }
        if (execNow) {
            mCurEnvStep.onEnterStep();
        }
        return true;

    }

    private EnvStep mCheckEnvStep = new EnvStep(ENV_STEP.CHECK) {

        @Override
        public void onStepHandle() {
            TsmCard card = mContext.getCard();
            if (card == null || TextUtils.isEmpty(card.getCPLC())) {
                switchStep(mGetCPLCStep);
                // todo判断服务端卡状态是否被拉取
            } else {
                switchStep(mFinalStep);
            }
        }
    };
    private EnvStep mGetCPLCStep = new EnvStep(ENV_STEP.GET_CPLC) {

        @Override
        public void onStepHandle() {
            if (!TextUtils.isEmpty(CacheUtil.getCacheCPLC())) {
                switchStep(mSyncServerStep);
                return;
            }
            ITsmCardChannel channel = mContext.getChannel();
            channel.getCPLC(new ITsmAPDUCallback() {

                @Override
                public void onSuccess(String[] apduList) {
                    // TODO 判断cplc合法性
                    TsmCard card = mContext.getCard();
                    card.setCPLC(apduList[0]);
                    switchStep(mSyncServerStep);
                }

                @Override
                public void onFail(int ret, String desc) {
                    notifyResult(ret, desc);
                    keepStep();
                }
            });

        }
    };
    private EnvStep mSyncServerStep = new EnvStep(ENV_STEP.SYNC_SERVER) {

        @Override
        public void onStepHandle() {
            SyncRemoteCardStatus remote = new SyncRemoteCardStatus(mContext);
            final long seqReq = remote.getUniqueSeq();
            remote.invoke(new IResponseObserver() {

                @Override
                public void onResponseSucceed(long uniqueSeq, int operType, JceStruct response) {
                    if (seqReq == uniqueSeq) {
                        CardStatusContextRsp rsp = (CardStatusContextRsp) response;
                        if (rsp.iRet != 0) {
                            notifyResult(rsp.iRet, null);
                            keepStep();
                            return;
                        }
                        mContext.syncCardStatus(rsp.context);
                        switchStep(mFinalStep);
                    }
                }

                @Override
                public void onResponseFailed(long uniqueSeq, int operType, int errorCode,
                        String description) {
                    if (seqReq == uniqueSeq) {
                        notifyResult(errorCode, null);
                        keepStep();
                    }
                }
            });
        }

    };
    private EnvStep mFinalStep = new EnvStep(ENV_STEP.FINAL) {

        @Override
        public void onStepHandle() {
            notifyResult(0, null);
        }
    };

    private void notifyResult(int ret, String desc) {
        if (mCallback == null) {
            return;
        }
        if (ret == 0) {
            mCallback.onSucess();
        } else {
            mCallback.onFail(ret, desc);
        }
    }
}
