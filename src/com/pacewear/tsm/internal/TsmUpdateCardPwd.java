
package com.pacewear.tsm.internal;

import android.text.TextUtils;

import com.pacewear.tsm.TsmConstants;
import com.pacewear.tsm.card.TsmContext;
import com.pacewear.tsm.server.tosservice.UpdateCardPwd;
import com.qq.taf.jce.JceStruct;

import java.util.List;

import TRom.E_APP_LIFE_STATUS;
import TRom.E_REPORT_APDU_KEY;
import TRom.E_SECURITY_DOMAIN_STATUS;
import TRom.SSDStatus;
import TRom.UpdateCardPwdRsp;

public class TsmUpdateCardPwd extends TsmBaseProcess {
    private String mSSDAID = null;

    public TsmUpdateCardPwd(TsmContext context, String sdAid) {
        super(context, TsmConstants.TSM_TYPE_UPDATEKEY);
        mSSDAID = sdAid;
    }

    @Override
    protected boolean onStart() {
        setProcessStatus(PROCESS_STATUS.WORKING);
        UpdateCardPwd updateCardPwd = new UpdateCardPwd(mContext, mSSDAID);
        boolean handle = process(updateCardPwd, new OnTsmProcessCallback() {

            @Override
            public void onSuccess(String[] apdus) {
                reportRet2Server(E_REPORT_APDU_KEY._ERAK_SECUTRITY_DOMAIN_KEY,
                        E_SECURITY_DOMAIN_STATUS._ESDS_PERSONALIZED, mSSDAID);
                setProcessStatus(PROCESS_STATUS.FINISH);
            }

            @Override
            public void onFail(int ret, String desc) {
                setProcessStatus(PROCESS_STATUS.KEEP);
            }
        });
        return handle;
    }

    @Override
    protected int onCheck() {
        int ret = CHECK_READY;
        SSDStatus ssdBaseStatus = mTsmCard.getSSDByAID(mSSDAID);
        switch (ssdBaseStatus.status) {
            case E_SECURITY_DOMAIN_STATUS._ESDS_INSTALLED:
            case E_SECURITY_DOMAIN_STATUS._ESDS_INITIALIZED:
                ret = CHECK_READY;
                break;
            case E_SECURITY_DOMAIN_STATUS._ESDS_LOCKED:
            case E_SECURITY_DOMAIN_STATUS._ESDS_DELETE:
                ret = CHECK_ERROR;
                break;
            default:
                ret = CHECK_SKIP;
                break;
        }
        return ret;
    }

    @Override
    protected int getApduList(JceStruct rsp, List<String> apdus, boolean fromLocal) {
        if (fromLocal) {
            return -1;
        }
        UpdateCardPwdRsp response = (UpdateCardPwdRsp) rsp;
        if (response == null || response.iRet != 0 || TextUtils.isEmpty(response.APDU)) {
            return -1;
        }
        apdus.add(response.APDU);
        return response.iRet;
    }

}
