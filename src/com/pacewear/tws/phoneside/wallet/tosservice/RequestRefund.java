
package com.pacewear.tws.phoneside.wallet.tosservice;

import com.qq.taf.jce.JceStruct;

import TRom.PayReqHead;
import TRom.RequestRefundReq;
import TRom.RequestRefundRsp;

public class RequestRefund extends PayTosService {
    private String mTradeNo = "";
    private String mSbTradeNo = "";

    public RequestRefund(String tradeNo) {
        mTradeNo = tradeNo;
    }

    public RequestRefund(String tradeNo, String sbTradeNo) {
        mTradeNo = tradeNo;
        mSbTradeNo = sbTradeNo;
    }

    @Override
    public String getFunctionName() {
        return "requestRefund";
    }

    @Override
    public int getOperType() {
        return OPERTYPE_REQUEST_REFUND;
    }

    @Override
    public JceStruct getReq(JceStruct _payReqHead) {
        PayReqHead payReqHead = (PayReqHead) _payReqHead;
        long curTime = System.currentTimeMillis();
        return new RequestRefundReq(payReqHead.stDeviceBaseInfo,
                payReqHead.stUserAuthInfo, payReqHead.stSEBaseInfo, mSbTradeNo, mTradeNo,
                curTime + "");
    }

    @Override
    public JceStruct getRspObject() {
        return new RequestRefundRsp();
    }

}
