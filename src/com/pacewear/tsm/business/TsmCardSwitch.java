
package com.pacewear.tsm.business;

import com.pacewear.tsm.card.TsmContext;
import com.pacewear.tsm.common.Constants;
import com.pacewear.tsm.internal.TsmActiveApp;
import com.pacewear.tsm.internal.TsmListState;
import com.pacewear.tsm.internal.TsmSelectAID;

public class TsmCardSwitch extends TsmBaseBusiness {
    private String mInstanceId = null;

    public TsmCardSwitch(TsmContext ctx, String aid) {
        super(ctx);
        mInstanceId = aid;
        skipEnvCheck();
    }

    @Override
    protected boolean onStart() {
        addProcess(new TsmSelectAID(mContext, mInstanceId));
        addProcess(new TsmSelectAID(mContext, Constants.TSM_CRS_AID));
        addProcess(new TsmListState(mContext, Constants.TSM_CRS_AID, 1)); // TODO
        addProcess(new TsmActiveApp(mContext, null, false));
        addProcess(new TsmActiveApp(mContext, mInstanceId, true));
        return true;
    }

}
