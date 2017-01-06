
package com.pacewear.tsm.server.tosservice;

import com.pacewear.tsm.card.TsmContext;
import com.qq.taf.jce.JceStruct;

import TRom.CommonRechargeReq;
import TRom.CommonRechargeRsp;
import TRom.RechargeExtraData;
import TRom.TSMHead;

public class AppletTopup extends TSMTosService {
    private String mAppletAID = null;
    private String mRspApdu = null;
    private RechargeExtraData mExtraData = null;

    public AppletTopup(TsmContext context, String aid) {
        super(context, "CommonRecharge", OPERTYPE_APPTOPUP);
        mAppletAID = aid;
    }

    public void setParam(String apdu, RechargeExtraData data) {
        mRspApdu = apdu;
        mExtraData = data;
    }

    @Override
    public JceStruct getRspObject() {
        return new CommonRechargeRsp();
    }

    @Override
    protected JceStruct getTsmReq(TSMHead head) {
        return new CommonRechargeReq(head, mAppletAID, mRspApdu, mExtraData);
    }

}
