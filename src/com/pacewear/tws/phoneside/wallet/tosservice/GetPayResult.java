
package com.pacewear.tws.phoneside.wallet.tosservice;

import TRom.E_PAY_TYPE;
import TRom.GetPayResultReq;
import TRom.GetPayResultRsp;
import TRom.PayReqHead;

import com.qq.taf.jce.JceStruct;

/**
 * @author baodingzhou
 */

public class GetPayResult extends TosService {

    private int mPayType = E_PAY_TYPE._E_PT_WEIXIN_PAY;

    private String mTradeNo = null;

    @Override
    public int getOperType() {
        return OPERTYPE_GET_PAY_RESULT;
    }

    @Override
    public String getFunctionName() {
        return "getPayResult";
    }

    public void setPayOrderParams(int payType, String tradeNo) {
        mPayType = payType;
        mTradeNo = tradeNo;
    }

    @Override
    public JceStruct getReq(PayReqHead payReqHead) {

        if (mTradeNo == null) {
            return null;
        }

        GetPayResultReq getPayResultReq = new GetPayResultReq(payReqHead.stDeviceBaseInfo,
                payReqHead.stUserAuthInfo, payReqHead.stSEBaseInfo, mPayType, mTradeNo);

        return getPayResultReq;
    }

    @Override
    public JceStruct getRspObject() {
        return new GetPayResultRsp();
    }

}
