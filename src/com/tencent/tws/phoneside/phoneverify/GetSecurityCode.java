
package com.tencent.tws.phoneside.phoneverify;

import com.qq.taf.jce.JceStruct;

import TRom.SecurityCodeReq;
import TRom.SecurityCodeRsp;

public class GetSecurityCode extends SmsTosService {
    private String mPhoneNum = null;
    private String mVerifyCode = null;

    public GetSecurityCode() {
        super(OPERTYPE_GET_VERIFYCODE, "getSecurityCode");
    }

    public void setParam(String phoneNum, String code) {
        mPhoneNum = phoneNum;
        mVerifyCode = code;
    }

    @Override
    public JceStruct getRspObject() {
        return new SecurityCodeRsp();
    }

    @Override
    protected JceStruct getSmsReq() {
        return new SecurityCodeReq(getDevInf(), getUsrInf(), mPhoneNum, mVerifyCode);
    }

}