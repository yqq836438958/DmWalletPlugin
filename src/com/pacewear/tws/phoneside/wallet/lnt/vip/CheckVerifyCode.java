
package com.pacewear.tws.phoneside.wallet.lnt.vip;

import com.qq.taf.jce.JceStruct;
import com.tencent.tws.phoneside.phoneverify.ISmsDataHandler;
import com.tencent.tws.phoneside.phoneverify.SmsModel;
import com.tencent.tws.phoneside.phoneverify.SmsTosService;

import TRom.LingnanPassReq;
import TRom.LingnanPassRsp;

public class CheckVerifyCode extends SmsTosService implements ISmsDataHandler {
    private String mPhoneNum = null;
    private String mVerifyCode = null;

    public CheckVerifyCode(String phoneNum, String code) {
        super(OPERTYPE_SEND_VERIFYCODE, "checkVerifyCode");
        mPhoneNum = phoneNum;
        mVerifyCode = code;
        mMouduleName = "WatchPayLingnanPass";
    }

    @Override
    public JceStruct getRspObject() {
        return new LingnanPassRsp();
    }

    @Override
    protected JceStruct getSmsReq() {
        return new LingnanPassReq(getDevInf(), getUsrInf(), mPhoneNum, mVerifyCode);
    }

    @Override
    public ParseResult onParse(JceStruct response) {
        LingnanPassRsp data = (LingnanPassRsp) response;
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
