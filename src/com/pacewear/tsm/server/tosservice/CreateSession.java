
package com.pacewear.tsm.server.tosservice;

import com.pacewear.tsm.card.TsmContext;
import com.qq.taf.jce.JceStruct;

import TRom.CreateSessionReq;
import TRom.CreateSessionRsp;
import TRom.TSMHead;

public class CreateSession extends TSMTosService {
    private int mStep = 0;
    private String mAPDU = null;
    private String mHostChanllege = null;
    private String mSSDAID = null;

    public CreateSession(TsmContext context) {
        super(context, "createSession", OPERTYPE_CREATESESSION);
    }

    public void setParams(int step, String aid, String apdu, String hostChanllege) {
        mStep = step;
        mSSDAID = aid;
        mAPDU = apdu;
        mHostChanllege = hostChanllege;
    }

    @Override
    protected JceStruct getTsmReq(TSMHead head) {
        CreateSessionReq req = new CreateSessionReq(head, mSSDAID, mStep, mAPDU,
                mHostChanllege,
                getSessionId());
        return req;
    }

    @Override
    public JceStruct getRspObject() {
        return new CreateSessionRsp();
    }
}
