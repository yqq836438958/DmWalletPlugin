
package com.pacewear.tsm.server.tosservice;

import android.text.TextUtils;
import android.util.Log;

import com.pacewear.httpserver.BaseTosService;
import com.pacewear.tsm.card.TsmCard;
import com.pacewear.tsm.card.TsmContext;
import com.pacewear.tsm.common.UniSessionStore;
import com.pacewear.tsm.constants.PayNFCConstants;
import com.qq.taf.jce.JceStruct;
import com.tencent.tws.phoneside.device.wup.DeviceInfoWupDataFactory;
import com.tencent.tws.phoneside.framework.RomBaseInfoHelper;

import TRom.CardBaseInfo;
import TRom.DeviceBaseInfo;
import TRom.TSMHead;

public abstract class TSMTosService extends BaseTosService {
    public static final int OPERTYPE_CREATESESSION = OPERTYPE_PULL_USER_INFO + 1;
    public static final int OPERTYPE_DELAPPLET = OPERTYPE_CREATESESSION + 1;
    public static final int OPERTYPE_GETCARDINFO = OPERTYPE_DELAPPLET + 1;
    public static final int OPERTYPE_INSTALLAPP = OPERTYPE_GETCARDINFO + 1;
    public static final int OPERTYPE_REPORTAPDU = OPERTYPE_INSTALLAPP + 1;
    public static final int OPERTYPE_UPDATECARDPWD = OPERTYPE_REPORTAPDU + 1;
    public static final int OPERTYPE_INSTALLSDD = OPERTYPE_UPDATECARDPWD + 1;
    public static final int OPERTYPE_SYNCCARDCONTEXT = OPERTYPE_INSTALLSDD + 1;
    public static final int OPERTYPE_LISTSTATE = OPERTYPE_SYNCCARDCONTEXT + 1;
    public static final int OPERTYPE_SETAPPSTATUS = OPERTYPE_LISTSTATE + 1;
    public static final int OPERTYPE_APPPERSONAL = OPERTYPE_SETAPPSTATUS + 1;
    public static final int OPERTYPE_RESET = OPERTYPE_APPPERSONAL + 1;
    public static final int OPERTYPE_APPINFOQUERY = OPERTYPE_RESET + 1;
    public static final int OPERTYPE_APPTOPUP = OPERTYPE_APPINFOQUERY + 1;
    public static final String REQ_NAME_TSM = "stReq";
    public static final String RSP_NAME_TSM = "stRsp";
    public static final String TAG = TSMTosService.class.getSimpleName();
    private String mFuncName = "";
    private int mOperType = OPERTYPE_UNKNOWN;
    protected TsmCard mTsmCard = null;

    public TSMTosService(TsmContext context, String functionName, int opertype) {
        super(PayNFCConstants.WUP.MODULE_NAME_TSM, REQ_NAME_TSM, RSP_NAME_TSM);
        mFuncName = functionName;
        mOperType = opertype;
        mTsmCard = context.getCard();
    }

    @Override
    public String getFunctionName() {
        return mFuncName;
    }

    @Override
    public int getOperType() {
        return mOperType;
    }

    @Override
    public JceStruct getReq(JceStruct jce) {
        TSMHead head = (TSMHead) jce;
        return getTsmReq(head);
    }

    protected abstract JceStruct getTsmReq(TSMHead head);

    @Override
    protected JceStruct getJceHeader() {
        if (mTsmCard == null) {
            return null;
        }
        String cplc = mTsmCard.getCPLC();
        if (TextUtils.isEmpty(cplc)) {
            Log.e(TAG, "getTSMHead error");
            return null;
        }
        DeviceBaseInfo deviceBaseInfo = getDeviceBaseInfo();
        CardBaseInfo cardBaseInfo = new CardBaseInfo(cplc);
        return new TSMHead(deviceBaseInfo, cardBaseInfo);
    }

    private DeviceBaseInfo getDeviceBaseInfo() {
        DeviceBaseInfo tmp = new DeviceBaseInfo();
        tmp.stPhoneBaseInfo = RomBaseInfoHelper.getRomBaseInfo();
        tmp.stWatchBaseInfo = DeviceInfoWupDataFactory.getInstance().getWatchRomBaseInfo();
        return tmp;
    }

    protected String getSessionId() {
        return UniSessionStore.getInstance().getId();
    }
}
