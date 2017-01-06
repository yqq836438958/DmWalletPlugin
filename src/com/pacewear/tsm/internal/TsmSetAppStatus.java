
package com.pacewear.tsm.internal;

import com.pacewear.tsm.card.TsmContext;
import com.pacewear.tsm.server.tosservice.SetAppletState;
import com.qq.taf.jce.JceStruct;

import java.util.List;

import TRom.SetAppletStatusRsp;

public class TsmSetAppStatus extends TsmBaseProcess {
    private String mAppletAid = null;
    private int mAppStatus = 0;

    public TsmSetAppStatus(TsmContext context, String aid, int status) {
        super(context, 0);
        mAppletAid = aid;
        mAppStatus = status;
    }

    @Override
    protected int onCheck() {
        return CHECK_READY;
    }

    @Override
    protected boolean onStart() {
        setProcessStatus(PROCESS_STATUS.WORKING);
        SetAppletState setStatus = new SetAppletState(mContext, mAppletAid);
        setStatus.setParam(mAppStatus);
        process(setStatus, new OnTsmProcessCallback() {

            @Override
            public void onSuccess(String[] apduList) {
                setProcessStatus(PROCESS_STATUS.FINISH);
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
        if (fromLocal) {
            return -1;
        }
        SetAppletStatusRsp data = (SetAppletStatusRsp) rsp;
        if (data.iRet != 0) {
            return data.iRet;
        }
        apdus.add(data.APDU);
        return 0;
    }

}
