
package com.pacewear.tsm.server.tosservice;

import com.pacewear.tsm.card.TsmContext;
import com.qq.taf.jce.JceStruct;

import TRom.InstallAppletReq;
import TRom.InstallAppletRsp;
import TRom.TSMHead;

public class InstallApplet extends TSMTosService {
    public InstallApplet(TsmContext context) {
        super(context, "InstallApplet", OPERTYPE_INSTALLAPP);
    }

    private String mAID = null;
    private int mStep = 0;

    public void setParams(int step, String aid) {
        mStep = step;
        mAID = aid;
    }

    @Override
    protected JceStruct getTsmReq(TSMHead head) {
        return new InstallAppletReq(head, mStep, mAID, getSessionId());
    }

    @Override
    public InstallAppletRsp getRspObject() {
        return new InstallAppletRsp();
    }

}
