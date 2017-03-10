
package com.pacewear.tws.phoneside.wallet.lnt.vip;

import com.qq.taf.jce.JceStruct;
import com.tencent.tws.phoneside.phoneverify.ISmsDataHandler;
import com.tencent.tws.phoneside.phoneverify.SmsTosService;

import TRom.LingnanPassReq;
import TRom.LingnanPassRsp;

public class SyncUserInfo extends SmsTosService implements ISmsDataHandler {
    private String mPhoneNum = null;
    private String mVerifyCode = null;

    public SyncUserInfo(String phoneNum, String code) {
        super(OPERTYPE_GET_VERIFYCODE, "userInfoSync");
        mMouduleName = "WatchPayLingnanPass";
        mPhoneNum = phoneNum;
        mVerifyCode = code;
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
        // data.iRet,0代表，无需进行下一步；1代表需要执行下一步,为了方便转换，这里颠倒一下
        // ParseResult.iRet为0的时候，代表正常流程；ParseResult.iRet为1的时候，代表省略步骤
        int iRet = data.iRet;
        if (data.iRet == 0) {
            iRet = 1;
        } else if (data.iRet == 1) {
            iRet = 0;
        }
        return ParseResult.newInstance(iRet, null);
    }

    @Override
    public int onPostHandle(String msg) {
        // TODO Auto-generated method stub
        return 0;
    }

}
