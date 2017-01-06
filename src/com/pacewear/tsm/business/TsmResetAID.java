
package com.pacewear.tsm.business;

import com.pacewear.httpserver.IResponseObserver;
import com.pacewear.tsm.ITsmBusinessListener;
import com.pacewear.tsm.card.TsmContext;
import com.pacewear.tsm.server.tosservice.ResetAID;
import com.qq.taf.jce.JceStruct;

import TRom.ResetRsp;

public class TsmResetAID extends TsmBaseBusiness {
    private String mAID = null;
    private ITsmBusinessListener mListener = null;

    public TsmResetAID(TsmContext ctx, String aid) {
        super(ctx);
//        this.skipEnvCheck();
        mAID = aid;
        mListener = mContext.getITsmBusinessListener();
    }

    @Override
    protected boolean onStart() {
        ResetAID reset = new ResetAID(mContext, mAID);
        final long lSeq = reset.getUniqueSeq();
        reset.invoke(new IResponseObserver() {

            @Override
            public void onResponseSucceed(long uniqueSeq, int operType, JceStruct response) {
                if (lSeq == uniqueSeq) {
                    ResetRsp data = (ResetRsp) response;
                    if (mListener != null) {
                        mListener.onSuccess("result:" + data.iRet);
                    }
                }
            }

            @Override
            public void onResponseFailed(long uniqueSeq, int operType, int errorCode,
                    String description) {
                if (lSeq == uniqueSeq) {
                    if (mListener != null) {
                        mListener.onFail(errorCode, description);
                    }
                }
            }
        });
        return false;
    }

}
