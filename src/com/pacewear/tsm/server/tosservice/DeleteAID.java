
package com.pacewear.tsm.server.tosservice;

import com.pacewear.tsm.card.TsmContext;
import com.qq.taf.jce.JceStruct;

import TRom.DeleteReq;
import TRom.DeleteRsp;
import TRom.TSMHead;

public class DeleteAID extends TSMTosService {
    public DeleteAID(TsmContext context) {
        super(context, "Delete", OPERTYPE_DELAPPLET);
    }

    public String mAID;

    public void setParams(String aid) {
        mAID = aid;
    }

    @Override
    protected JceStruct getTsmReq(TSMHead head) {
        return new DeleteReq(head, mAID, getSessionId());
    }

    @Override
    public JceStruct getRspObject() {
        return new DeleteRsp();
    }
}
