
package com.pacewear.tws.phoneside.wallet.tosservice;

import TRom.ApplyRefundReq;
import TRom.ApplyRefundRsp;
import TRom.E_PAY_SCENE;
import TRom.E_PAY_TYPE;
import TRom.GetPayResultRspParam;
import TRom.OrderReqParam;
import TRom.OrderRspParam;
import TRom.PayReqHead;
import TRom.RefunReqParam;

import com.pacewear.httpserver.BaseTosService;
import com.qq.taf.jce.JceStruct;

/**
 * @author baodingzhou
 */

public class ApplyRefund extends PayTosService {

    private OrderReqParam mOrderReqParam = null;

    private OrderRspParam mOrderRspParam = null;

    private GetPayResultRspParam mGetPayResultRspParam = null;

    // TODO
    // 退款金额 不应该客户端来决定
    private long mRefoundFee = 0;

    @Override
    public int getOperType() {
        return OPERTYPE_GET_PAY_RESULT;
    }

    @Override
    public String getFunctionName() {
        return "applyRefund";
    }

    public ApplyRefund setOrderReqParam(OrderReqParam orderReqParam) {
        mOrderReqParam = orderReqParam;
        return this;
    }

    public ApplyRefund setOrderRspParam(OrderRspParam orderRspParam) {
        mOrderRspParam = orderRspParam;
        return this;
    }

    public ApplyRefund setGetPayResultRspParam(GetPayResultRspParam getPayResultRspParam) {
        mGetPayResultRspParam = getPayResultRspParam;
        return this;
    }

    public ApplyRefund setRefoundFee(long refoundFee) {
        mRefoundFee = refoundFee;
        return this;
    }

    @Override
    public JceStruct getReq(JceStruct _payReqHead) {
        PayReqHead payReqHead = (PayReqHead) _payReqHead;
        if (payReqHead == null) {
            return null;
        }

        if (mOrderReqParam == null) {
            return null;
        }

        if (mOrderRspParam == null) {
            return null;
        }

        if (mGetPayResultRspParam == null) {
            return null;
        }

        // 1:issue 2:topup 3:issue and topup
        String refoundFeeType = "3";
        switch (mOrderReqParam.ePayScene) {
            case E_PAY_SCENE._EPS_STAT:
                refoundFeeType = "2";
                break;
            case E_PAY_SCENE._EPS_OPEN_CARD:
            case E_PAY_SCENE._EPS_OPEN_CARD_ONLY:
            default:
                refoundFeeType = "3";
                break;
        }

        RefunReqParam refunReqParam = new RefunReqParam(mOrderReqParam.sDeviceInfo,
                mGetPayResultRspParam.sOutTradeNO,
                mRefoundFee,
                refoundFeeType, mOrderRspParam.sTradeNo);

        ApplyRefundReq req = new ApplyRefundReq(payReqHead.stDeviceBaseInfo,
                payReqHead.stUserAuthInfo, payReqHead.stSEBaseInfo,
                mOrderReqParam.stBusCardBaseInfo, mOrderReqParam.ePayType, refunReqParam);

        return req;
    }

    @Override
    public JceStruct getRspObject() {
        return new ApplyRefundRsp();
    }

}
