
package com.pacewear.tws.phoneside.wallet.tosservice;

import TRom.GetCardStatusReq;
import TRom.GetCardStatusRsp;
import TRom.PayReqHead;

import com.qq.taf.jce.JceStruct;

/**
 * @author baodingzhou
 */

public class LatestCardStatus extends TosService {

    @Override
    public int getOperType() {
        return OPERTYPE_GET_CARD_STATUS;
    }

    @Override
    public JceStruct getReq(PayReqHead payReqHead) {
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
