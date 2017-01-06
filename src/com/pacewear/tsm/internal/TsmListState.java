
package com.pacewear.tsm.internal;

import android.text.TextUtils;

import com.pacewear.tsm.card.TsmContext;
import com.pacewear.tsm.server.tosservice.ListState;
import com.qq.taf.jce.JceStruct;

import java.util.List;

import TRom.ListStatusRsp;

public class TsmListState extends TsmBaseProcess {
    private boolean mCanStopTransmit = false;
    private String mSSDAID = null;
    private int mCarElement = 0;
    private String mPostAPDU = null;
    private ListState listState = null;

    public TsmListState(TsmContext context, String ssdAID, int cardElement) {
        super(context, 0);
        mSSDAID = ssdAID;
        mCarElement = cardElement;
    }

    @Override
    protected boolean onStart() {
        setProcessStatus(PROCESS_STATUS.WORKING);
        if (listState == null) {
            listState = new ListState(mContext, mSSDAID);
        }
        listState.setParam(mPostAPDU, mCarElement);
        process(listState, new OnTsmProcessCallback() {

            @Override
            public void onSuccess(String[] apduList) {
                if (mCanStopTransmit) {
                    setProcessStatus(PROCESS_STATUS.FINISH);
                } else {
                    mPostAPDU = apduList[0];
                    setProcessStatus(PROCESS_STATUS.REPEAT);
                }
            }

            @Override
            public void onFail(int ret, String desc) {
                setProcessStatus(PROCESS_STATUS.KEEP);
            }
        });

        return true;
    }

    @Override
    protected int onCheck() {
        return CHECK_READY;
    }

    @Override
    protected int getApduList(JceStruct rsp, List<String> apdus, boolean fromLocal) {
        if (fromLocal) {
            return -1;
        }
        ListStatusRsp listStatus = (ListStatusRsp) rsp;
        if (listStatus.iRet != 0) {
            return -1;
        }
        if (listStatus.hasNext == 0) {
            mCanStopTransmit = true;
        }
        if (!TextUtils.isEmpty(listStatus.APDU)) {
            apdus.add(listStatus.APDU);
        }
        return 0;
    }

    @Override
    protected boolean returnWithoutTransmit() {
        return mCanStopTransmit;
    }
}
