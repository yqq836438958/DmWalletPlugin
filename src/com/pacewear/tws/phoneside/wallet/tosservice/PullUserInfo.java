
package com.pacewear.tws.phoneside.wallet.tosservice;

import TRom.GetCustomServiceReq;
import TRom.GetCustomServiceRsp;
import TRom.PayReqHead;

import com.qq.taf.jce.JceStruct;

public class PullUserInfo extends PayTosService {

    private static final String TAG = PullUserInfo.class.getSimpleName();

    @Override
    public String getFunctionName() {
        return "getCustomService";
    }

    @Override
    public JceStruct getReq(JceStruct _payReqHead) {
        PayReqHead payReqHead = (PayReqHead) _payReqHead;
        return new GetCustomServiceReq(payReqHead.getStDeviceBaseInfo(), payReqHead.stUserAuthInfo,
                payReqHead.getStSEBaseInfo());
    }

    @Override
    public JceStruct getRspObject() {
        return new GetCustomServiceRsp();
    }

    public int getOperType() {
        return OPERTYPE_PULL_USER_INFO;
    }

}
