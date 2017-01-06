
package com.pacewear.tsm.server.tosservice;

import com.pacewear.tsm.card.TsmContext;
import com.qq.taf.jce.JceStruct;

import TRom.CardStatusContextReq;
import TRom.CardStatusContextRsp;
import TRom.TSMHead;

public class SyncRemoteCardStatus extends TSMTosService {

    public SyncRemoteCardStatus(TsmContext context) {
        super(context, "SyncRemoteCardStatus", OPERTYPE_SYNCCARDCONTEXT);
    }

    @Override
    protected JceStruct getTsmReq(TSMHead head) {
        return new CardStatusContextReq(head);
    }

    @Override
    public JceStruct getRspObject() {
        return new CardStatusContextRsp();
    }

}
