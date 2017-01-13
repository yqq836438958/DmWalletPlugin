
package com.pacewear.tsm.internal;

import com.pacewear.tsm.card.TsmContext;
import com.pacewear.tsm.common.Constants;
import com.pacewear.tsm.internal.core.OnTsmProcessCallback;
import com.pacewear.tsm.internal.core.TsmBaseProcess;
import com.pacewear.tsm.server.tosservice.AppPersonal;
import com.qq.taf.jce.JceStruct;

import java.util.List;

import TRom.AppletStatus;
import TRom.CommonPersonalRsp;
import TRom.E_APP_LIFE_STATUS;

public class TsmPersonal extends TsmBaseProcess {
    private String mToken = null;
    private String mExtra_Info = null;
    private AppPersonal mPersonService = null;
    private String mExecAPDUResult = null;
    private boolean mPersonalFinish = false;

    public TsmPersonal(TsmContext context, String appid, String token, String extraInfo) {
        super(context, appid, false);
        mToken = token;
        mExtra_Info = extraInfo;
    }

    @Override
    protected int onCheck() {
        AppletStatus status = mTsmCard.getAppletByAID(mContainerAID);
        if (status == null) {
            return CHECK_ERROR;
        }
        int ret = CHECK_READY;
        switch (status.status) {
            case E_APP_LIFE_STATUS._EALS_PERSONALIZED:
                ret = CHECK_SKIP;
                break;
            case E_APP_LIFE_STATUS._EALS_LOCKED:
                ret = CHECK_ERROR;
                break;
            default:
                break;
        }
        return ret;
    }

    @Override
    protected boolean onStart() {
        setProcessStatus(PROCESS_STATUS.WORKING);
        if (mPersonService == null) {
            mPersonService = new AppPersonal(mContext, mContainerAID, mToken, mExtra_Info);
        }
        mPersonService.setApdu(mExecAPDUResult);
        process(mPersonService, new OnTsmProcessCallback() {

            @Override
            public void onSuccess(String[] apduList) {
                if (mPersonalFinish) {
                    mTsmCard.updateCardListItemInstallStat(mContainerAID,
                            Constants.TSM_APP_PERSONAL);
                    setProcessStatus(PROCESS_STATUS.FINISH);
                } else {
                    mExecAPDUResult = apduList[0];// TODO
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
    protected int onParse(JceStruct rsp, List<String> apdus, boolean fromLocal) {
        if (fromLocal) {
            return -1;
        }
        CommonPersonalRsp data = (CommonPersonalRsp) rsp;
        if (data.iRet != 0) {
            return data.iRet;
        }
        mPersonalFinish = data.bFinishPersonal;
        apdus.addAll(data.vAPDU);
        return 0;
    }
}
