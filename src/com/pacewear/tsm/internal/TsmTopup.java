
package com.pacewear.tsm.internal;

import com.pacewear.tsm.card.TsmContext;
import com.pacewear.tsm.internal.TsmBaseProcess.PROCESS_STATUS;
import com.pacewear.tsm.server.tosservice.AppletTopup;
import com.pacewear.tsm.server.tosservice.ListState;
import com.qq.taf.jce.JceStruct;

import java.util.List;

import TRom.AppletStatus;
import TRom.CommonRechargeRsp;
import TRom.E_APP_LIFE_STATUS;
import TRom.RechargeExtraData;

public class TsmTopup extends TsmBaseProcess {
    private boolean mStopTramsit = false;
    private String mAppAid = null;
    private AppletTopup mAppletTopup = null;
    private String mPostAPDU = null;
    private RechargeExtraData mExtraDat = null;

    public TsmTopup(TsmContext context, String aid, String token, String extraInfo) {
        super(context, 0);
        mAppAid = aid;
        mExtraDat = new RechargeExtraData(token, extraInfo);
    }

    @Override
    protected int onCheck() {
        AppletStatus status = mContext.getCard().getAppletByAID(mAppAid);
        if (status == null) {
            return CHECK_ERROR;
        }
        int ret = CHECK_READY;
        switch (status.status) {
            case E_APP_LIFE_STATUS._EALS_INSTALL_FOR_MAKESELECT:
            case E_APP_LIFE_STATUS._EALS_PERSONALIZED:
                ret = CHECK_READY;
                break;
            default:
                ret = CHECK_ERROR;
                break;
        }
        return ret;
    }

    @Override
    protected boolean onStart() {
        setProcessStatus(PROCESS_STATUS.WORKING);
        if (mAppletTopup == null) {
            mAppletTopup = new AppletTopup(mContext, mAppAid);
        }
        mAppletTopup.setParam(mPostAPDU, mExtraDat);
        process(mAppletTopup, new OnTsmProcessCallback() {

            @Override
            public void onSuccess(String[] apduList) {
                if (mStopTramsit) {
                    setProcessStatus(PROCESS_STATUS.FINISH);
                } else {
                    mPostAPDU = apduList[0];
                    setProcessStatus(PROCESS_STATUS.REPEAT);
                }
            }

            @Override
            public void onFail(int ret, String desc) {
                setProcessStatus(PROCESS_STATUS.KEEP);
            }
        });

        return true;
    }

    @Override
    protected int getApduList(JceStruct rsp, List<String> apdus, boolean fromLocal) {
        if (fromLocal == true) {
            return -1;
        }
        CommonRechargeRsp data = (CommonRechargeRsp) rsp;
        if (data == null) {
            return -1;
        }
        if (data.iRet != 0) {
            return -1;
        }
        mStopTramsit = data.bFinishPersonal;
        apdus.addAll(data.vAPDU);
        return 0;
    }

    @Override
    protected boolean returnWithoutTransmit() {
        return mStopTramsit;
    }
}
