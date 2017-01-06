
package com.pacewear.tsm.server.tosservice;

import com.pacewear.tsm.card.TsmContext;
import com.pacewear.tsm.common.UniSessionStore;
import com.qq.taf.jce.JceStruct;

import TRom.SetAppletStatusReq;
import TRom.SetAppletStatusRsp;
import TRom.TSMHead;

public class SetAppletState extends TSMTosService {
    private String mAppletAID = null;
    private int mSetStatus = 0;

    public SetAppletState(TsmContext context, String appid) {
        super(context, "SetAppletStatus", OPERTYPE_SETAPPSTATUS);
        mAppletAID = appid;
    }

    public void setParam(int eSetStatus) {
        mSetStatus = eSetStatus;
    }

    @Override
    public JceStruct getRspObject() {
        return new SetAppletStatusRsp();
    }

    @Override
    protected JceStruct getTsmReq(TSMHead head) {
        SetAppletStatusReq req = new SetAppletStatusReq(head, mAppletAID, mSetStatus,
                getSessionId());
        return req;
    }

}
