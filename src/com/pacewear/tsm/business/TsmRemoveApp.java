
package com.pacewear.tsm.business;

import com.pacewear.tsm.card.TsmContext;
import com.pacewear.tsm.internal.TsmDeleteAID;
import com.pacewear.tsm.internal.TsmOpenSession;

public class TsmRemoveApp extends TsmBaseBusiness {
    private String mBusinessAID = null;

    public TsmRemoveApp(TsmContext ctx, String aid) {
        super(ctx);
        mBusinessAID = aid;
    }

    @Override
    protected boolean onStart() {
        String mainAid = mContext.getCard().getMainAID();
        addProcess(new TsmOpenSession(mContext, mainAid));
        addProcess(new TsmDeleteAID(mContext, mBusinessAID));
        return true;
    }

}
