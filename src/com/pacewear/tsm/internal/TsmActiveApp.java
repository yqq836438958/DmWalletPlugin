
package com.pacewear.tsm.internal;

import com.pacewear.tsm.card.TsmCard;
import com.pacewear.tsm.card.TsmContext;
import com.pacewear.tsm.common.APDUUtil;
import com.qq.taf.jce.JceStruct;

import java.util.List;

public class TsmActiveApp extends TsmBaseProcess {
    private boolean mActive = false;
    private String mAid = null;

    public TsmActiveApp(TsmContext context, String aid, boolean isActive) {
        super(context, 0);
        mAid = aid;
        mActive = isActive;
    }

    @Override
    protected int onCheck() {
        if (!mActive) {
            TsmCard card = mContext.getCard();
            mAid = card.getActiveAID();
        }
        return 0;
    }

    @Override
    protected boolean onStart() {
        setProcessStatus(PROCESS_STATUS.WORKING);
        process(null, new OnTsmProcessCallback() {

            @Override
            public void onSuccess(String[] apduList) {
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
    protected int getApduList(JceStruct rsp, List<String> apdus, boolean fromLoacal) {
        if (mActive) {
            apdus.add(APDUUtil.activeApp(mAid));
        } else {
            apdus.add(APDUUtil.disactiveApp(mAid));
        }
        return 0;
    }

}
