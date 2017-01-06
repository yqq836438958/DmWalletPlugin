
package com.pacewear.tsm.server.tosservice;

import com.pacewear.tsm.card.TsmContext;
import com.pacewear.tsm.common.UniSessionStore;
import com.qq.taf.jce.JceStruct;

import TRom.TSMHead;
import TRom.UpdateCardPwdReq;
import TRom.UpdateCardPwdRsp;

public class UpdateCardPwd extends TSMTosService {
    public String mSSDAID = null;

    public UpdateCardPwd(TsmContext context, String ssdaid) {
        super(context, "UpdateCardPwd", OPERTYPE_UPDATECARDPWD);
        mSSDAID = ssdaid;
    }

    @Override
    protected JceStruct getTsmReq(TSMHead head) {
        return new UpdateCardPwdReq(head, getSessionId(), mSSDAID);
    }

    @Override
    public JceStruct getRspObject() {
        return new UpdateCardPwdRsp();
    }
}
