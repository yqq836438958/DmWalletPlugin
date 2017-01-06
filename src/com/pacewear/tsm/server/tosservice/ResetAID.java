
package com.pacewear.tsm.server.tosservice;

import com.pacewear.tsm.card.TsmContext;
import com.qq.taf.jce.JceStruct;

import TRom.ResetReq;
import TRom.ResetRsp;
import TRom.TSMHead;

public class ResetAID extends TSMTosService {
    private String mAid = null;

    public ResetAID(TsmContext context, String aid) {
        super(context, "Reset", OPERTYPE_RESET);
        mAid = aid;
    }

    @Override
    public JceStruct getRspObject() {
        return new ResetRsp();
    }

    @Override
    protected JceStruct getTsmReq(TSMHead head) {
        return new ResetReq(head, mAid);
    }

}
