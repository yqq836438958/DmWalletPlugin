
package com.pacewear.tsm.server.tosservice;

import com.pacewear.tsm.card.TsmContext;
import com.qq.taf.jce.JceStruct;

import TRom.AppInfoQueryReq;
import TRom.AppInfoQueryRsp;
import TRom.TSMHead;

public class AppInfoQuery extends TSMTosService {
    private String mAid = null;

    public AppInfoQuery(TsmContext context, String aid) {
        super(context, "AppInfoQuery", OPERTYPE_APPINFOQUERY);
        mAid = aid;
    }

    @Override
    public JceStruct getRspObject() {
        return new AppInfoQueryRsp();
    }

    @Override
    protected JceStruct getTsmReq(TSMHead head) {
        return new AppInfoQueryReq(head, mAid);
    }

}
