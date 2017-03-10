
package com.pacewear.tsm.internal;

import android.util.Log;

import com.pacewear.tsm.card.TsmContext;
import com.pacewear.tsm.internal.core.OnTsmProcessCallback;
import com.pacewear.tsm.internal.core.TsmBaseProcess;
import com.pacewear.tsm.server.tosservice.SetAppletState;
import com.qq.taf.jce.JceStruct;

import java.util.List;

import TRom.SetAppletStatusRsp;

public class TsmSetAppStatus extends TsmBaseProcess {
    private int mAppStatus = 0;
    private String mBusinessAID = null;

    public TsmSetAppStatus(TsmContext context, String containerAID, String aid, int status) {
        super(context, containerAID, false);
        mBusinessAID = aid;
        mAppStatus = status;
    }

    @Override
    protected int onCheck() {
        return CHECK_READY;
    }

    @Override
    protected boolean onStart() {
        setProcessStatus(PROCESS_STATUS.WORKING);
        SetAppletState setStatus = new SetAppletState(mContext, mBusinessAID);
        setStatus.setParam(mAppStatus);
        process(setStatus, new OnTsmProcessCallback() {

            @Override
            public void onSuccess(String[] apduList) {
                setProcessStatus(PROCESS_STATUS.FINISH);
            }

            @Override
            public void onFail(int ret, String desc) {
                Log.e(TAG, "TsmSetAppStatus fail:" + ret + ",desc:" +
                        desc);
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
        SetAppletStatusRsp data = (SetAppletStatusRsp) rsp;
        if (data.iRet != 0) {
            return data.iRet;
        }
        apdus.add(data.APDU);
        return 0;
    }

}
