
package com.tencent.tws.phoneside.phoneverify.common;

import com.qq.taf.jce.JceStruct;
import com.tencent.tws.phoneside.phoneverify.ISmsDataHandler;
import com.tencent.tws.phoneside.phoneverify.SmsModel;
import com.tencent.tws.phoneside.phoneverify.SmsTosService;

import TRom.SecurityCodeReq;
import TRom.SecurityCodeRsp;

public class SendSecurityCode extends SmsTosService implements ISmsDataHandler {
    private String mPhoneNum = null;
    private String mVerifyCode = null;

    public SendSecurityCode(String phoneNum, String code) {
        super(OPERTYPE_SEND_VERIFYCODE, "sendSecurityCode");
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

    @Override
    public ParseResult onParse(JceStruct response) {
        SecurityCodeRsp data = (SecurityCodeRsp) response;
        if (data == null) {
            return ParseResult.newInstance(-1, null);
        }
        return ParseResult.newInstance(data.iRet, null);
    }

    @Override
    public int onPostHandle(String msg) {
        SmsModel.get().saveGlobalPhoneNum(msg);
        return 0;
    }
}
