
package com.pacewear.tsm.internal;

import com.pacewear.tsm.card.TsmContext;
import com.pacewear.tsm.common.APDUUtil;
import com.pacewear.tsm.internal.core.OnTsmProcessCallback;
import com.pacewear.tsm.internal.core.TsmBaseProcess;
import com.qq.taf.jce.JceStruct;

import java.util.List;

public class TsmActiveApp extends TsmBaseProcess {
    private boolean mToAct = false;
    private String mToActAID = null;

    public TsmActiveApp(TsmContext context, String containerAID, String aid, boolean isActive) {
        super(context, containerAID, false);
        mToActAID = aid;
        mToAct = isActive;
    }

    @Override
    protected int onCheck() {
        boolean isCurActive = mTsmCard.isAIDActive(mToActAID);
        if (mToAct == isCurActive) {
            return CHECK_SKIP;
        }
        return CHECK_READY;
    }

    @Override
    protected boolean onStart() {
        setProcessStatus(PROCESS_STATUS.WORKING);
        process(null, new OnTsmProcessCallback() {

            @Override
            public void onSuccess(String[] apduList) {
                if (mToAct) {
                    mTsmCard.activeAID(mToActAID);
                } else {
                    mTsmCard.unactiveAID(mToActAID);
                }
                setProcessStatus(PROCESS_STATUS.FINISH);
            }

            @Override
            public void onFail(int ret, String desc) {
                setProcessStatus(PROCESS_STATUS.KEEP);
            }
        });
        return true;
    }

    @Override
    protected int onParse(JceStruct rsp, List<String> apdus, boolean fromLoacal) {
        if (mToAct) {
            apdus.add(APDUUtil.activeApp(mToActAID));
        } else {
            apdus.add(APDUUtil.disactiveApp(mToActAID));
        }
        return 0;
    }

}
