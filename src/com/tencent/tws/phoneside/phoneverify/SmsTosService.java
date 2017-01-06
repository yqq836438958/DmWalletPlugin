
package com.tencent.tws.phoneside.phoneverify;

import com.pacewear.httpserver.BaseTosService;
import com.qq.taf.jce.JceStruct;
import com.tencent.tws.phoneside.business.AccountManager;
import com.tencent.tws.phoneside.device.wup.DeviceInfoWupDataFactory;
import com.tencent.tws.phoneside.framework.RomBaseInfoHelper;

import TRom.DeviceBaseInfo;
import TRom.RomAccountInfo;
import TRom.UserAuthInfo;

public abstract class SmsTosService extends BaseTosService {
    private String mFuncName = "";
    private int mOpertype = OPERTYPE_UNKNOWN;

    public SmsTosService(int opertype, String functionName) {
        super();
        mOpertype = opertype;
        mFuncName = functionName;
        mMouduleName = "watchsms";
        mNeedReqHeader = false;
    }

    @Override
    public int getOperType() {
        return mOpertype;
    }

    @Override
    public String getFunctionName() {
        return mFuncName;
    }

    @Override
    public JceStruct getReq(JceStruct payReqHead) {
        return getSmsReq();
    }

    protected final DeviceBaseInfo getDevInf() {
        DeviceBaseInfo tmp = new DeviceBaseInfo();
        tmp.stPhoneBaseInfo = RomBaseInfoHelper.getRomBaseInfo();
        tmp.stWatchBaseInfo = DeviceInfoWupDataFactory.getInstance().getWatchRomBaseInfo();
        return tmp;
    }

    protected final UserAuthInfo getUsrInf() {
        RomAccountInfo accountInfo = AccountManager.getInstance().getLoginAccountIdInfo();
        return new UserAuthInfo(accountInfo);
    }

    protected abstract JceStruct getSmsReq();

    protected JceStruct getJceHeader() {
        return null;
    }
}
