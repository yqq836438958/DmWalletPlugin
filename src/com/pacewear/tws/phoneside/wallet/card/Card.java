
package com.pacewear.tws.phoneside.wallet.card;

import android.text.TextUtils;

import com.google.gson.JsonObject;
import com.pacewear.tws.phoneside.wallet.WalletApp;
import com.pacewear.tws.phoneside.wallet.common.Utils;
import com.pacewear.tws.phoneside.wallet.step.IStep;
import com.pacewear.tws.phoneside.wallet.step.IStep.COMMON_STEP;
import com.pacewear.tws.phoneside.wallet.step.IStep.STATUS;
import com.pacewear.tws.phoneside.wallet.step.Step;
import com.pacewear.tws.phoneside.wallet.walletservice.CardQuery;
import com.pacewear.tws.phoneside.wallet.walletservice.CardSwitch;
import com.pacewear.tws.phoneside.wallet.walletservice.IResult;
import com.tencent.tws.proto.wallet.WatchNFCManager.MSG_STATE;

import java.util.HashMap;
import java.util.Map.Entry;

import qrom.component.log.QRomLog;

/**
 * @author baodingzhou
 */

public abstract class Card implements ICard, ICardInner {
    protected CONFIG mConfig = null;

    private INSTALL_STATUS mInstallStatus = INSTALL_STATUS.UNINSTALLED;

    private ACTIVATION_STATUS mActivationStatus = ACTIVATION_STATUS.UNACTIVATED;

    private String mCardName = null;

    protected String mCardInfoErrDesc = null;

    private HashMap<String,String> mExtraInfoMap = new HashMap<String,String>();

    private IStep<COMMON_STEP> mCurrentCardStep = null;

    private IStep<COMMON_STEP> mCurrentCardSwitchStep = null;

    public Card() {
    }

    protected String getTAG() {
        return "Card";
    }

    @Override
    public final void setConfig(CONFIG config) {
        if (mConfig == null) {
            mConfig = config;

            mCardName = WalletApp.sGlobalCtx.getString(mConfig.mCardNameRes);

            // Final
            mCurrentCardStep = mCardUnavaiable;
            mCurrentCardSwitchStep = mSwitchStepUnavaiable;
        }
    }

    @Override
    public final CARD_TYPE getCardType() {
        return mConfig.mType;
    }

    @Override
    public final String getAID() {
        return mConfig.mAID;
    }

    @Override
    public final INSTALL_STATUS getInstallStatus() {
        return mInstallStatus;
    }

    @Override
    public final void setInstallStatus(INSTALL_STATUS installStatus) {
        if (mInstallStatus != installStatus) {
            mInstallStatus = installStatus;
            notifyModuleStepStatus(COMMON_STEP.UPDATED, STATUS.HANDLE);
        }
    }

    @Override
    public final ACTIVATION_STATUS getActivationStatus() {
        return mActivationStatus;
    }

    @Override
    public final void setActivationStatus(ACTIVATION_STATUS activationStatus) {
        if (mActivationStatus != activationStatus) {
            mActivationStatus = activationStatus;
            notifyModuleStepStatus(COMMON_STEP.UPDATED, STATUS.HANDLE);
        }
    }

    @Override
    public final String getCardName() {
        return mCardName;
    }

    /**
     * 强制更新
     */
    private void forceUpdateInner() {
        if (!mCardReady.isCurrentStep()) {
            mCurrentCardStep.onStep();
        } else {
            setStep(mCardDubious);
        }
    }

    /**
     * 设置默认卡
     */
    private final void setDefaultCardInner() {
        if (!mCurrentCardSwitchStep.isCurrentStep()) {
            mCurrentCardSwitchStep.onStep();
        } else {
            setSwitchStep(mSwitchStepDubious);
        }
    }

    private final boolean setStep(IStep<COMMON_STEP> step) {

        boolean handle = false;

        if (step == null) {
            return handle;
        }

        if (mCurrentCardStep != step) {
            IStep<COMMON_STEP> previousStep = mCurrentCardStep;
            mCurrentCardStep = step;
            if (previousStep != null) {
                previousStep.onQuitStep();
            }
            mCurrentCardStep.onEnterStep();
            handle = true;
        }

        return handle;
    }

    private final boolean setSwitchStep(IStep<COMMON_STEP> step) {
        boolean handle = false;
        if (step == null) {
            return handle;
        }

        if (mCurrentCardSwitchStep != step) {
            IStep<COMMON_STEP> previousStep = mCurrentCardSwitchStep;
            mCurrentCardSwitchStep = step;
            if (previousStep != null) {
                previousStep.onQuitStep();
            }
            mCurrentCardSwitchStep.onEnterStep();
            handle = true;
        } else {
            mCurrentCardSwitchStep.onStep();
        }
        return handle;
    }

    private abstract class CardStep extends Step<COMMON_STEP> {

        private long mUniqueReq = -1;

        public CardStep(COMMON_STEP step) {
            super(step);

            // Initialized status
            if (step == COMMON_STEP.UNAVAILABLE) {
                mStatus = STATUS.KEEP;
            }
        }

        @Override
        protected final boolean setStep(IStep<COMMON_STEP> step) {
            return Card.this.setStep(step);
        }

        @Override
        protected final void notifyStepStatus(COMMON_STEP step, STATUS status) {
            notifyModuleStepStatus(step, status);
        }

        protected final boolean queryCardInfo() {
            QRomLog.d(getTAG(), "cardQuery " + getAID());

            CardQuery cardQuery = new CardQuery();

            JsonObject params = null;
            params = new JsonObject();
            params.addProperty("instance_id", getAID());
            // params.addProperty("tag", "card_number,balance");

            cardQuery.putString(params.toString());
            mUniqueReq = cardQuery.getSeqID();
            return cardQuery.invoke(new IResult() {

                @Override
                public void onResult(long seqID, int ret, String[] outputParams,
                        Integer[] resultCode, byte[] bytes) {
                    String info = (outputParams != null && outputParams.length > 0)
                            ? outputParams[0] : null;
                    if (mUniqueReq == seqID) {
                        QRomLog.d(getTAG(),
                                String.format("CardQuery.onResult ret:%d info:%s", ret, info));
                        if (ret == 0 && info != null) {
                            if (parseCardInfo(info)) {
                                switchStep(mCardUpdated);
                            } else {
                                switchStep(mCardReady);
                            }
                        } else {
                            keepStep();
                        }
                        setCardInfoErrCode(ret);
                    }
                }

                @Override
                public void onExecption(long seqID, int error) {
                    if (mUniqueReq == seqID) {
                        QRomLog.d(getTAG(),
                                "CardQuery.onExecption error:" + MSG_STATE.convert(error));
                        keepStep();
                    }
                }
            });
        }
    };

    private final CardStep mCardUnavaiable = new CardStep(COMMON_STEP.UNAVAILABLE) {
        @Override
        public void onStepHandle() {
            queryCardInfo();
        }
    };

    private final CardStep mCardUpdated = new CardStep(COMMON_STEP.UPDATED) {
        @Override
        public void onStepHandle() {
            switchStep(mCardReady);
        }
    };

    private final CardStep mCardReady = new CardStep(COMMON_STEP.READY) {
        @Override
        public void onStepHandle() {
            // Do Nothing.
        }
    };

    private final CardStep mCardDubious = new CardStep(COMMON_STEP.DUBIOUS) {
        @Override
        public void onStepHandle() {
            queryCardInfo();
        }
    };

    private final void notifyModuleStepStatus(COMMON_STEP step, STATUS status) {
        CardManager.getInstanceInner().notifyCardMsg(getAID(), step, status);
    }

    private abstract class CardSwitchStep extends Step<COMMON_STEP> {
        private long mUniqueReq = -1;

        public CardSwitchStep(COMMON_STEP step) {
            super(step);
            if (step == COMMON_STEP.UNAVAILABLE) {
                mStatus = STATUS.KEEP;
            }
        }

        @Override
        protected boolean setStep(IStep<COMMON_STEP> step) {
            return Card.this.setSwitchStep(step);
        }

        @Override
        protected void notifyStepStatus(COMMON_STEP step, STATUS status) {
            // 成功时候不需要在这里告诉UI，交给CardManager去执行上报
            if (status == STATUS.KEEP) {
                notifyModuleStepStatus(step, status);
            }
        }

        protected boolean switchCard() {
            QRomLog.d(getTAG(), "switchCard " + getAID());

            CardSwitch cardSwitch = new CardSwitch();
            cardSwitch.putString(getAID());
            mUniqueReq = cardSwitch.getSeqID();

            return cardSwitch.invoke(new IResult() {

                @Override
                public void onResult(long seqID, int ret, String[] outputParams,
                        Integer[] resultCode, byte[] bytes) {
                    if (mUniqueReq == seqID) {
                        QRomLog.d(getAID(), "CardSwitch.onResult ret:" + ret);
                        if (ret == 0) {
                            switchStep(mSwitchStepUpdated);
                            CardManager.getInstance().forceUpdate(true);
                        } else {
                            keepStep();
                        }
                    }
                }

                @Override
                public void onExecption(long seqID, int error) {
                    if (mUniqueReq == seqID) {
                        QRomLog.d(getAID(),
                                "CardSwitch.onExecption error:" + MSG_STATE.convert(error));
                        keepStep();
                    }
                }
            });
        }
    };

    private final CardSwitchStep mSwitchStepReady = new CardSwitchStep(COMMON_STEP.READY) {
        @Override
        public void onStepHandle() {
            // do thing
        }
    };

    private final CardSwitchStep mSwitchStepUnavaiable = new CardSwitchStep(
            COMMON_STEP.UNAVAILABLE) {
        @Override
        public void onStepHandle() {
            switchCard();
        }
    };

    private final CardSwitchStep mSwitchStepDubious = new CardSwitchStep(COMMON_STEP.DUBIOUS) {
        @Override
        public void onStepHandle() {
            switchCard();
        }
    };

    private final CardSwitchStep mSwitchStepUpdated = new CardSwitchStep(COMMON_STEP.UPDATED) {
        @Override
        public void onStepHandle() {
            switchStep(mSwitchStepReady);
        }
    };

    @Override
    public boolean isReady() {
        return mCardReady.isCurrentStep();
    }

    @Override
    public boolean isAvaliable() {
        return !mCardUnavaiable.isCurrentStep();
    }

    @Override
    public void forceUpdate() {
        Utils.getWorkerHandler().post(new Runnable() {
            @Override
            public void run() {
                forceUpdateInner();
            }
        });
    }

    @Override
    public boolean setDefaultCard() {
        Utils.getWorkerHandler().post(new Runnable() {
            @Override
            public void run() {
                setDefaultCardInner();
            }
        });
        return true;
    }

    @Override
    public int getCardICon() {
        return mConfig.mCardIconRes;
    }

    @Override
    public int getCardBg() {
        return mConfig.mCardBgRes;
    }

    @Override
    public String getExtra_Info() {
        StringBuilder builder = new StringBuilder();
        boolean isEmpty = true;
        for (Entry<String, String> entry : mExtraInfoMap.entrySet()) {
            if(isEmpty){
                isEmpty = false;
            } else {
                builder = builder.append("&");
            }
            builder = builder.append(entry.getKey()).append("=").append(entry.getValue());
        }
        return builder.toString();
    }

    @Override
    public void setExtra_Info(String key, String value) {
        if (TextUtils.isEmpty(value) || TextUtils.isEmpty(key)) {
            return;
        }
        mExtraInfoMap.put(key, value);
    }

    /**
     * parseCardInfo
     * 
     * @param cardInfo
     * @return true for updated or false.
     */
    protected abstract boolean parseCardInfo(String cardInfo);

    @Override
    public void setCardInfoErrCode(int _code) {
        String today = Utils.getCurrentTime();
        String validity = ((ITrafficCard) this).getValidity();
        String startdate = ((ITrafficCard) this).getStartDate();
        int code = _code;
        if (!TextUtils.isEmpty(validity) && Utils.compareDate(today, validity) > 0) {
            // 超过有效期
            code = 430009;
        }
        if (!TextUtils.isEmpty(startdate) && Utils.compareDate(today, startdate) < 0) {
            // 未到启用日期
            code = 430010;
        }
        String desc = Utils.parseErrCodeDesc(code, getAID());
        mCardInfoErrDesc = desc;
    }

    @Override
    public String getCardInfoErrDesc() {
        return mCardInfoErrDesc;
    }
}
