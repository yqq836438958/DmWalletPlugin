
package com.pacewear.tsm.internal;

import android.text.TextUtils;

import com.pacewear.tsm.TsmConstants;
import com.pacewear.tsm.card.TsmContext;
import com.pacewear.tsm.server.tosservice.InstallSSD;
import com.qq.taf.jce.JceStruct;

import java.util.List;

import TRom.E_APP_LIFE_STATUS;
import TRom.E_INSTALL_SSD_STEP;
import TRom.E_REPORT_APDU_KEY;
import TRom.E_SECURITY_DOMAIN_STATUS;
import TRom.InstallSSDRsp;
import TRom.SSDStatus;

public class TsmSecDomInstall extends TsmBaseProcess {

    private String mSDAID = null;
    private int mStep = 0;

    public TsmSecDomInstall(TsmContext context, String aid, int step) {
        super(context, TsmConstants.TSM_TYPE_INSTALLSECDOM);
        mSDAID = aid;
        mStep = step;
    }

    @Override
    protected boolean onStart() {
        setProcessStatus(PROCESS_STATUS.WORKING);
        InstallSSD install = new InstallSSD(mContext, mSDAID);
        install.setStep(mStep);
        boolean handle = process(install, new OnTsmProcessCallback() {

            @Override
            public void onSuccess(String[] apduList) {
                if (mStep == E_INSTALL_SSD_STEP._EISS_INSTALL_FOR_INSTALL_MAKESELECT) {
                    reportRet2Server(E_REPORT_APDU_KEY._ERAK_SECUTRITY_DOMAIN_KEY,
                            E_SECURITY_DOMAIN_STATUS._ESDS_INSTALLED, mSDAID);
                } else if (mStep == E_INSTALL_SSD_STEP._EISS_INSTALL_FOR_EXTRADITION) {
                    reportRet2Server(E_REPORT_APDU_KEY._ERAK_SECUTRITY_DOMAIN_KEY,
                            E_SECURITY_DOMAIN_STATUS._ESDS_EXTRADITION, mSDAID);
                }
                setProcessStatus(PROCESS_STATUS.FINISH);

            }

            @Override
            public void onFail(int ret, String desc) {
                setProcessStatus(PROCESS_STATUS.KEEP);

            }
        });
        return handle;
    }

    @Override
    protected int onCheck() {
        SSDStatus ssdStatus = mTsmCard.getSSDByAID(mSDAID);
        int ret = CHECK_READY;
        switch (ssdStatus.status) {
            case E_SECURITY_DOMAIN_STATUS._ESDS_PERSONALIZED:
                if (mStep == E_INSTALL_SSD_STEP._EISS_INSTALL_FOR_INSTALL_MAKESELECT) {
                    ret = CHECK_SKIP;
                } else {
                    ret = CHECK_READY;
                }
                break;

            case E_SECURITY_DOMAIN_STATUS._ESDS_INSTALLED:
                ret = (mStep == E_INSTALL_SSD_STEP._EISS_INSTALL_FOR_INSTALL_MAKESELECT)
                        ? CHECK_SKIP : CHECK_READY;
                break;
            case E_SECURITY_DOMAIN_STATUS._ESDS_EXTRADITION:
                ret = CHECK_SKIP;
                break;
            case E_SECURITY_DOMAIN_STATUS._ESDS_DELETE:
            case E_SECURITY_DOMAIN_STATUS._ESDS_INITIALIZED:
                ret = CHECK_READY;
                break;
            case E_SECURITY_DOMAIN_STATUS._ESDS_LOCKED:
                ret = CHECK_ERROR;
                break;
            default:
                ret = CHECK_READY;
                break;
        }
        return ret;
    }

    @Override
    protected int getApduList(JceStruct rsp, List<String> apdus, boolean fromLocal) {
        if (fromLocal == true) {
            return -1;
        }
        InstallSSDRsp response = (InstallSSDRsp) rsp;
        if (TextUtils.isEmpty(response.APDU)) {
            return -1;
        }
        apdus.add(response.APDU);
        return response.iRet;
    }
}
