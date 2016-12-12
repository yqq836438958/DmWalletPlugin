
package com.tencent.tws.phoneside.phoneverify;

import com.qq.taf.jce.JceStruct;

import TRom.PhoneNumberReq;
import TRom.PhoneNumberRsp;

public class GetPhoneNumber extends SmsTosService {
    public GetPhoneNumber() {
        super(OPERTYPE_GET_PHONENUM, "getPhoneNumber");
    }

    @Override
    public JceStruct getRspObject() {
        return new PhoneNumberRsp();
    }

    @Override
    protected JceStruct getSmsReq() {
        return new PhoneNumberReq(getDevInf(), getUsrInf());
    }

}
