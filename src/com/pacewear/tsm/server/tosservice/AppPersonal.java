
package com.pacewear.tsm.server.tosservice;

import com.pacewear.tsm.card.TsmContext;
import com.qq.taf.jce.JceStruct;

import TRom.AppletPersonalData;
import TRom.CommonPersonalReq;
import TRom.CommonPersonalRsp;
import TRom.TSMHead;

public class AppPersonal extends TSMTosService {
    private String mAID = null;
    private String mAPDU = null;
    private String mToken = null;
    private String mExtraInfo = null;

    public AppPersonal(TsmContext context, String aid, String token, String extra_info) {
        super(context, "CommonPersonal", OPERTYPE_APPPERSONAL);
        mAID = aid;
        mToken = token;
        mExtraInfo = extra_info;
    }

    @Override
    public JceStruct getRspObject() {
        return new CommonPersonalRsp();
    }

    public void setApdu(String apdu) {
        mAPDU = apdu;
    }

    @Override
    protected JceStruct getTsmReq(TSMHead head) {
        AppletPersonalData personalData = new AppletPersonalData(mToken, mExtraInfo);
        return new CommonPersonalReq(head, mAID, mAPDU, personalData);
    }

}
