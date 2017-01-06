
package com.pacewear.tsm.server.tosservice;

import com.pacewear.tsm.card.TsmContext;
import com.qq.taf.jce.JceStruct;

import TRom.GetCardMngReq;
import TRom.GetCardMngRsp;
import TRom.TSMHead;

public class GetCardMngInfo extends TSMTosService {
    public GetCardMngInfo(TsmContext context) {
        super(context, "getCardMngInfo", OPERTYPE_GETCARDINFO);
    }

    @Override
    public JceStruct getRspObject() {
        return new GetCardMngRsp();
    }

    @Override
    protected JceStruct getTsmReq(TSMHead head) {
        return new GetCardMngReq(head);
    }

}
