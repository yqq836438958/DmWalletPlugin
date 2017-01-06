
package com.pacewear.tsm.server.tosservice;

import com.pacewear.tsm.card.TsmContext;
import com.pacewear.tsm.common.UniSessionStore;
import com.qq.taf.jce.JceStruct;

import TRom.ListStatusReq;
import TRom.ListStatusRsp;
import TRom.TSMHead;

public class ListState extends TSMTosService {
    private String mSSDAID = null;
    private int mCardElement = 0;
    private String mAPDU = null;

    public ListState(TsmContext context, String ssdAID) {
        super(context, "ListStatus", OPERTYPE_LISTSTATE);
        mSSDAID = ssdAID;
    }

    public void setParam(String apdu, int cardElement) {
        mAPDU = apdu;
        mCardElement = cardElement;
    }

    @Override
    public JceStruct getRspObject() {
        return new ListStatusRsp();
    }

    @Override
    protected JceStruct getTsmReq(TSMHead head) {
        return new ListStatusReq(head, mAPDU, mSSDAID, mCardElement, getSessionId());
    }

}
