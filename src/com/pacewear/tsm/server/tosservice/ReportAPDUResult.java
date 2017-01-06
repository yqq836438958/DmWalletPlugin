
package com.pacewear.tsm.server.tosservice;

import com.pacewear.tsm.card.TsmContext;
import com.qq.taf.jce.JceStruct;

import TRom.ReportAPDUResultReq;
import TRom.ReportAPDUResultRsp;
import TRom.TSMHead;

public class ReportAPDUResult extends TSMTosService {

    private String mAID = null;
    private int mReportKey = 0;
    private int mStatus = 0;

    public ReportAPDUResult(TsmContext context) {
        super(context, "ReportAPDUResult", OPERTYPE_REPORTAPDU);
    }

    public void setParams(int resultType, int result, String aid) {
        mReportKey = resultType;
        mStatus = result;
        mAID = aid;
    }

    @Override
    protected JceStruct getTsmReq(TSMHead head) {
        return new ReportAPDUResultReq(head, mAID, mReportKey, mStatus);
    }

    @Override
    public JceStruct getRspObject() {
        return new ReportAPDUResultRsp();
    }

}
