
package com.pacewear.tws.phoneside.wallet.tosservice;

import TRom.PayBusCardConfigReq;
import TRom.PayBusCardConfigRsp;
import TRom.PayReqHead;

import com.qq.taf.jce.JceStruct;

/**
 * @author baodingzhou
 */

public class BusCardConfig extends TosService {

    private String mLastMD5 = null;

    public void setmLastMD5(String lastMD5) {
        this.mLastMD5 = lastMD5;
    }

    @Override
    public int getOperType() {
        return OPERTYPE_GET_PAY_BUSCARD_CONFIG_LIST;
    }

    @Override
    public JceStruct getReq(PayReqHead payReqHead) {

        PayBusCardConfigReq req = new PayBusCardConfigReq(payReqHead, null);
        if (mLastMD5 != null) {
            req.sMd5 = mLastMD5;
        }

        return req;
    }

    @Override
    public String getFunctionName() {
        return "getPayBusCardConfigList";
    }

    @Override
    public JceStruct getRspObject() {
        return new PayBusCardConfigRsp();
    }
}
