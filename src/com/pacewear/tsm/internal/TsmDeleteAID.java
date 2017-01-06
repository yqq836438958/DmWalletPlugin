
package com.pacewear.tsm.internal;

import android.text.TextUtils;

import com.pacewear.tsm.TsmConstants;
import com.pacewear.tsm.card.TsmCard;
import com.pacewear.tsm.card.TsmContext;
import com.pacewear.tsm.server.tosservice.DeleteAID;
import com.qq.taf.jce.JceStruct;

import java.util.List;

import TRom.AppletStatus;
import TRom.DeleteRsp;
import TRom.E_APP_LIFE_STATUS;
import TRom.E_REPORT_APDU_KEY;
import TRom.E_SECURITY_DOMAIN_STATUS;
import TRom.SSDStatus;

public class TsmDeleteAID extends TsmBaseProcess {
    private String mAppletAid = null;

    public TsmDeleteAID(TsmContext context, String aid) {
        super(context, TsmConstants.TSM_TYPE_DELETEAPPLET);
        mAppletAid = aid;
    }

    @Override
    protected boolean onStart() {
        setProcessStatus(PROCESS_STATUS.WORKING);
        DeleteAID deleteApplet = new DeleteAID(mContext);
        deleteApplet.setParams(mAppletAid);
        boolean handle = process(deleteApplet, new OnTsmProcessCallback() {

            @Override
            public void onSuccess(String[] apdus) {
                if (isSDAID()) {
                    reportRet2Server(E_REPORT_APDU_KEY._ERAK_SECUTRITY_DOMAIN_KEY,
                            E_SECURITY_DOMAIN_STATUS._ESDS_DELETE, mAppletAid);
                } else {
                    reportRet2Server(E_REPORT_APDU_KEY._ERAK_APP_KEY,
                            E_APP_LIFE_STATUS._EALS_DELETE, mAppletAid);
                }
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
        if (isSDAID()) {
            return checkSSDStatus();
        }
        return checkAppStatus();
    }

    @Override
    protected int getApduList(JceStruct rsp, List<String> apdus, boolean fromLocal) {
        if (fromLocal) {
            return -1;
        }
        DeleteRsp response = (DeleteRsp) rsp;
        if (response.iRet != 0 || TextUtils.isEmpty(response.APDU)) {
            return -1;
        }
        apdus.add(response.APDU);
        return response.iRet;
    }

    private boolean isSDAID() {
        TsmCard card = mContext.getCard();
        if (card.getSSDByAID(mAppletAid) != null) {
            return true;
        }
        return false;
    }

    private int checkAppStatus() {
        int ret = CHECK_READY;
        AppletStatus appletStatus = mTsmCard.getAppletByAID(mAppletAid);
        if (appletStatus == null) {
            return CHECK_SKIP;
        }
        switch (appletStatus.status) {
            case E_APP_LIFE_STATUS._EALS_INSTALL_FOR_MAKESELECT:
            case E_APP_LIFE_STATUS._EALS_LOAD:
            case E_APP_LIFE_STATUS._EALS_PERSONALIZED:
            case E_APP_LIFE_STATUS._EALS_LOCKED:
                ret = CHECK_READY;
                break;
            default:
                ret = CHECK_SKIP;
                break;
        }
        return ret;
    }

    private int checkSSDStatus() {
        int ret = CHECK_READY;
        SSDStatus ssdStatus = mTsmCard.getSSDByAID(mAppletAid);
        if (ssdStatus == null) {
            return CHECK_SKIP;
        }
        switch (ssdStatus.status) {
            case E_SECURITY_DOMAIN_STATUS._ESDS_EXTRADITION:
            case E_SECURITY_DOMAIN_STATUS._ESDS_INSTALLED:
            case E_SECURITY_DOMAIN_STATUS._ESDS_LOCKED:
            case E_SECURITY_DOMAIN_STATUS._ESDS_PERSONALIZED:
                ret = CHECK_READY;
                break;
            case E_SECURITY_DOMAIN_STATUS._ESDS_DELETE:
                ret = CHECK_SKIP;
                break;
            default:
                ret = CHECK_ERROR;
                break;
        }
        return ret;
    }
}
