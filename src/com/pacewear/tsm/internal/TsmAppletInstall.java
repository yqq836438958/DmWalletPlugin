
package com.pacewear.tsm.internal;

import android.text.TextUtils;

import com.pacewear.tsm.TsmConstants;
import com.pacewear.tsm.card.TsmContext;
import com.pacewear.tsm.common.Constants;
import com.pacewear.tsm.internal.core.OnTsmProcessCallback;
import com.pacewear.tsm.internal.core.TsmBaseProcess;
import com.pacewear.tsm.server.tosservice.InstallApplet;
import com.pacewear.tsm.step.IStep;
import com.pacewear.tsm.step.Step;
import com.qq.taf.jce.JceInputStream;
import com.qq.taf.jce.JceStruct;

import java.util.ArrayList;
import java.util.List;

import TRom.AppletStatus;
import TRom.E_APP_LIFE_STATUS;
import TRom.E_INSTALL_APPLET_STEP;
import TRom.E_REPORT_APDU_KEY;
import TRom.InstallAPDU;
import TRom.InstallAppletRsp;
import qrom.component.wup.base.utils.ZipUtils;

public class TsmAppletInstall extends TsmBaseProcess {

    public enum INSTALL_STEP {
        EIAPS_INSTALL_FORLOAD, EIAPS_LOAD, EIAPS_INSTALL_FOR_INSTALL_AND_MAKE_SELECTABLE, EALS_FINAL,
    }

    private IStep<INSTALL_STEP> mCurStep = null;
    private String mInstall_LoadApdu = null;
    private ArrayList<String> mLoadApdus = null;
    private ArrayList<String> mInstallApdu = null;
    private String mBusinessAID = null;

    public TsmAppletInstall(TsmContext context, String containerAID, String appletAID) {
        super(context, containerAID, true);
        mBusinessAID = appletAID;
        setInstallStep(mInstall_LoadStep, false);
    }

    private boolean setInstallStep(IStep<INSTALL_STEP> step, boolean execNow) {
        if (step == null) {
            return false;
        }

        if (mCurStep != step) {
            IStep<INSTALL_STEP> previousStep = mCurStep;
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

    private abstract class InstallStep extends Step<INSTALL_STEP> {

        public InstallStep(INSTALL_STEP step) {
            super(step);
        }

        @Override
        protected boolean setStep(IStep<INSTALL_STEP> step) {
            return setInstallStep(step, true);
        }

        @Override
        protected void notifyStepStatus(INSTALL_STEP step,
                STATUS status) {

        }
    }

    private InstallStep mInstall_LoadStep = new InstallStep(INSTALL_STEP.EIAPS_INSTALL_FORLOAD) {

        @Override
        public void onStepHandle() {
            InstallApplet installApplet = new InstallApplet(mContext);
            installApplet.setParams(E_INSTALL_APPLET_STEP._EALS_INSTALL_ALL_LOAD_APDU,
                    mBusinessAID);
            process(installApplet, new OnTsmProcessCallback() {

                @Override
                public void onSuccess(String[] apdu) {
                    switchStep(mLoadStep);
                }

                @Override
                public void onFail(int ret, String desc) {
                    keepStep();
                }
            });
        }

    };
    private InstallStep mLoadStep = new InstallStep(INSTALL_STEP.EIAPS_LOAD) {

        @Override
        public void onStepHandle() {
            InstallApplet load = new InstallApplet(mContext);
            load.setParams(E_INSTALL_APPLET_STEP._EALS_INSTALL_ALL_LOAD_APDU,
                    mBusinessAID);
            process(load, new OnTsmProcessCallback() {

                @Override
                public void onSuccess(String[] apdu) {
                    reportRet2Server(E_REPORT_APDU_KEY._ERAK_APP_KEY,
                            E_APP_LIFE_STATUS._EALS_LOAD, mBusinessAID);
                    switchStep(mInstall_Install_Selected);
                }

                @Override
                public void onFail(int ret, String desc) {
                    keepStep();
                }
            });
        }

    };
    private InstallStep mInstall_Install_Selected = new InstallStep(
            INSTALL_STEP.EIAPS_INSTALL_FOR_INSTALL_AND_MAKE_SELECTABLE) {

        @Override
        public void onStepHandle() {
            InstallApplet install = new InstallApplet(mContext);
            install.setParams(E_INSTALL_APPLET_STEP._EALS_INSTALL_ALL_LOAD_APDU,
                    mBusinessAID);
            process(install, new OnTsmProcessCallback() {

                @Override
                public void onSuccess(String[] apdu) {
                    switchStep(mFinalStep);
                }

                @Override
                public void onFail(int ret, String desc) {
                    keepStep();
                }
            });
        }
    };

    private InstallStep mFinalStep = new InstallStep(INSTALL_STEP.EALS_FINAL) {

        @Override
        public void onStepHandle() {
            mTsmCard.updateCardListItemInstallStat(mBusinessAID, Constants.TSM_APP_INSTALL);
            reportRet2Server(E_REPORT_APDU_KEY._ERAK_APP_KEY,
                    E_APP_LIFE_STATUS._EALS_INSTALL_FOR_MAKESELECT, mBusinessAID);
            finishProcess();
        }
    };

    @Override
    protected boolean onStart() {
        return setInstallStep(mCurStep, true);
    }

    @Override
    public boolean isFinish() {
        return mFinalStep.isCurrentStep();
    }

    @Override
    public boolean isIdle() {
        return mCurStep == null;
    }

    private boolean isNotEmpty(String str) {
        return !TextUtils.isEmpty(str);
    }

    private int getLocalAPDUS(List<String> apdus) {
        int ret = 0;
        switch (mCurStep.getStep()) {
            case EIAPS_INSTALL_FORLOAD:
                if (isNotEmpty(mInstall_LoadApdu)) {
                    apdus.add(mInstall_LoadApdu);
                }
                break;
            case EIAPS_LOAD:
                if (mLoadApdus != null && mLoadApdus.size() > 0) {
                    apdus.addAll(mLoadApdus);
                }
                break;
            case EIAPS_INSTALL_FOR_INSTALL_AND_MAKE_SELECTABLE:
                if (mInstallApdu != null && !mInstallApdu.isEmpty()) {
                    apdus.addAll(mInstallApdu);
                }
                break;
            default:
                ret = -1;
                break;
        }
        return ret;
    }

    @Override
    protected int onCheck() {
        int ret = CHECK_READY;
        AppletStatus appletStatus = mTsmCard.getAppletByAID(mBusinessAID);
        if (appletStatus == null) {
            return CHECK_ERROR;
        }
        switch (appletStatus.status) {
            case E_APP_LIFE_STATUS._EALS_LOAD:
                setInstallStep(mInstall_Install_Selected, false);
                break;
            case E_APP_LIFE_STATUS._EALS_INSTALL_FOR_MAKESELECT:
            case E_APP_LIFE_STATUS._EALS_PERSONALIZED:
                ret = CHECK_SKIP;
                break;
            case E_APP_LIFE_STATUS._EALS_LOCKED:
                ret = CHECK_ERROR;
                break;
            default:
                ret = CHECK_READY;
                break;
        }
        return ret;
    }

    @Override
    protected int onParse(JceStruct rsp, List<String> apdus, boolean fromLocal) {
        if (fromLocal) {
            return getLocalAPDUS(apdus);
        }
        InstallAppletRsp data = (InstallAppletRsp) rsp;
        if (data.iRet != 0) {
            return data.iRet;
        }
        byte[] unzipApdu = null;
        unzipApdu = ZipUtils.unGzip(data.vData);
        if (unzipApdu == null) {
            return -1;
        }
        InstallAPDU apduList = new InstallAPDU();
        JceInputStream jceInputStream = new JceInputStream(unzipApdu);
        apduList = (InstallAPDU) jceInputStream.read(apduList, 0, false);
        if (apduList == null) {
            return -1;
        }
        mInstall_LoadApdu = apduList.sInstallForLoadAPDU;
        mLoadApdus = apduList.sLoadAPDU;
        mInstallApdu = apduList.vInstallAndMakeSelectablbeAPDU;
        switch (mCurStep.getStep()) {
            case EIAPS_INSTALL_FORLOAD:
                apdus.add(apduList.sInstallForLoadAPDU);
                break;
            case EIAPS_LOAD:
                apdus.addAll(apduList.sLoadAPDU);
                break;
            case EIAPS_INSTALL_FOR_INSTALL_AND_MAKE_SELECTABLE:
                apdus.addAll(apduList.vInstallAndMakeSelectablbeAPDU);
                break;
            default:
                break;
        }
        return data.iRet;
    }

}
