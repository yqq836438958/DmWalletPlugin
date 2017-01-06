
package com.pacewear.tws.phoneside.wallet.tosservice;

import TRom.OrderReqParam;
import TRom.PayReqHead;
import TRom.UnifiedOrderReq;
import TRom.UnifiedOrderRsp;

import com.qq.taf.jce.JceStruct;

/**
 * @author baodingzhou
 */

public class UnifiedOrder extends PayTosService {

    private OrderReqParam mOrderReqParam = null;

    @Override
    public int getOperType() {
        return OPERTYPE_UNIFIED_ORDER;
    }

    @Override
    public String getFunctionName() {
        return "unifiedOrder";
    }

    public void setOrderReqParam(OrderReqParam orderReqParam) {
        mOrderReqParam = orderReqParam;
    }

    @Override
    public JceStruct getReq(JceStruct _payReqHead) {
        PayReqHead payReqHead = (PayReqHead) _payReqHead;
        if (mOrderReqParam == null) {
            return null;
        }

        UnifiedOrderReq unifiedOrderReq = new UnifiedOrderReq(payReqHead.stDeviceBaseInfo,
                payReqHead.stUserAuthInfo, payReqHead.stSEBaseInfo, mOrderReqParam);

        return unifiedOrderReq;
    }

    @Override
    public JceStruct getRspObject() {
        return new UnifiedOrderRsp();
    }

    @Override
    protected boolean getRequestEncrypt() {
        return true;
    }
}
