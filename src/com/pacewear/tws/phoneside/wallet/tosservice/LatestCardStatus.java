
package com.pacewear.tws.phoneside.wallet.tosservice;

import TRom.GetCardStatusReq;
import TRom.GetCardStatusRsp;
import TRom.PayReqHead;

import com.qq.taf.jce.JceStruct;

/**
 * @author baodingzhou
 */

public class LatestCardStatus extends PayTosService {

    @Override
    public int getOperType() {
        return OPERTYPE_GET_CARD_STATUS;
    }

    @Override
    public JceStruct getReq(JceStruct _payReqHead) {
        PayReqHead payReqHead = (PayReqHead) _payReqHead;
        GetCardStatusReq req = new GetCardStatusReq(payReqHead.stDeviceBaseInfo,
                payReqHead.stUserAuthInfo, payReqHead.stSEBaseInfo);

        return req;
    }

    @Override
    public String getFunctionName() {
        return "getLatestCardStatus";
    }

    @Override
    public JceStruct getRspObject() {
        return new GetCardStatusRsp();
    }
}
