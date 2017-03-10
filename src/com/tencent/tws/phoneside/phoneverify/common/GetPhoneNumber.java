
package com.tencent.tws.phoneside.phoneverify.common;

import android.text.TextUtils;

import com.qq.taf.jce.JceStruct;
import com.tencent.tws.phoneside.phoneverify.ISmsDataHandler;
import com.tencent.tws.phoneside.phoneverify.SmsModel;
import com.tencent.tws.phoneside.phoneverify.SmsTosService;

import TRom.PhoneNumberReq;
import TRom.PhoneNumberRsp;

public class GetPhoneNumber extends SmsTosService implements ISmsDataHandler {
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

    @Override
    public ParseResult onParse(JceStruct response) {
        PhoneNumberRsp data = (PhoneNumberRsp) response;
        if (data == null) {
            return ParseResult.newInstance(-1, null);
        }
        return ParseResult.newInstance(data.iRet, data.sPhoneNum);
    }

    @Override
    public int onPostHandle(String msg) {
        SmsModel.get().saveGlobalPhoneNum(msg);
        return 0;
    }

}
