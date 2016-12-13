
package com.tencent.tws.phoneside.phoneverify;

import com.pacewear.tws.phoneside.wallet.tosservice.TosService;
import com.qq.taf.jce.JceStruct;
import com.tencent.tws.phoneside.business.AccountManager;
import com.tencent.tws.phoneside.device.wup.DeviceInfoWupDataFactory;
import com.tencent.tws.phoneside.framework.RomBaseInfoHelper;

import TRom.DeviceBaseInfo;
import TRom.PayReqHead;
import TRom.RomAccountInfo;
import TRom.UserAuthInfo;

public abstract class SmsTosService extends TosService {
    private String mFuncName = "";
    private int mOpertype = OPERTYPE_UNKNOWN;

    public SmsTosService(int opertype, String functionName) {
        super();
        mOpertype = opertype;
        mFuncName = functionName;
        mFromOutMoudle = true;
        mMoudleName = "watchsms";
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
    public JceStruct getReq(PayReqHead payReqHead) {
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
}
