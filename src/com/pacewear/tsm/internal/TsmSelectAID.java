
package com.pacewear.tsm.internal;

import com.pacewear.tsm.TsmConstants;
import com.pacewear.tsm.business.TsmBaseBusiness.IBusinessInterceptListener;
import com.pacewear.tsm.card.TsmCard;
import com.pacewear.tsm.card.TsmContext;
import com.pacewear.tsm.channel.ITsmAPDUCallback;
import com.pacewear.tsm.common.ByteUtil;
import com.qq.taf.jce.JceStruct;

import java.util.List;

public class TsmSelectAID extends TsmBaseProcess {
    private String mToSelectAID = null;

    public TsmSelectAID(TsmContext context, String aid, IBusinessInterceptListener callback) {
        super(context, TsmConstants.TSM_TYPE_SELECTAID, callback);
        mToSelectAID = aid;
    }

    public TsmSelectAID(TsmContext context, String aid) {
        super(context, TsmConstants.TSM_TYPE_SELECTAID);
        mToSelectAID = aid;
    }

    @Override
    protected boolean onStart() {
        setProcessStatus(PROCESS_STATUS.WORKING);
        // TsmCard card = mContext.getCard();
        // AppletStatus appletStatus = card.getAppletByAID(mToSelectAID);
        // if(appletStatus != null && appletStatus.AID.equalsIgnoreCase(mToSelectAID)){
        // mToSelectAID = ByteUtil.parseAscii(mToSelectAID);
        // }
        return getChannel().selectAID(mToSelectAID, new ITsmAPDUCallback() {

            @Override
            public void onSuccess(String[] apdus) {
                TsmCard card = mContext.getCard();
                card.setActiveAID(mToSelectAID);
                setProcessStatus(PROCESS_STATUS.FINISH);
            }

            @Override
            public void onFail(int ret, String desc) {
                setProcessStatus(PROCESS_STATUS.KEEP);
                postHandleFail(ret, desc);
            }
        });
    }

    @Override
    protected int onCheck() {
        return CHECK_READY;
    }

    @Override
    protected int getApduList(JceStruct rsp, List<String> apdus, boolean fromLocal) {
        return 0;
    }

}
