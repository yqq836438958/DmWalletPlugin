
package com.pacewear.tsm.server.tosservice;

import com.pacewear.tsm.card.TsmContext;
import com.qq.taf.jce.JceStruct;

import TRom.InstallSSDReq;
import TRom.InstallSSDRsp;
import TRom.TSMHead;

public class InstallSSD extends TSMTosService {
    private String sAID = null;
    private int mStep = 0;

    public InstallSSD(TsmContext context, String aid) {
        super(context, "InstallSSD", OPERTYPE_INSTALLSDD);
        sAID = aid;
    }

    public void setStep(int step) {
        mStep = step;
    }

    @Override
    public JceStruct getRspObject() {
        return new InstallSSDRsp();
    }

    @Override
    protected JceStruct getTsmReq(TSMHead head) {
        return new InstallSSDReq(head, sAID, getSessionId(), mStep);
    }

}
