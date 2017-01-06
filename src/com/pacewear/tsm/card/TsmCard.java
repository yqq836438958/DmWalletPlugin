
package com.pacewear.tsm.card;

import com.pacewear.tsm.business.TsmBusinessConfig;
import com.pacewear.tsm.common.Constants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import TRom.AppletStatus;
import TRom.CardStatusContext;
import TRom.SSDStatus;

public class TsmCard {
    private String mCPLC = null;
    private String mMainAID = Constants.TSM_DEFAULT_CARDMAIN_AID;;
    private List<AppletStatus> mAppletList = new ArrayList<AppletStatus>();
    private List<SSDStatus> mSSDList = new ArrayList<SSDStatus>();
    private JSONArray mAppletWhiteList = new JSONArray();
    private String mCurActAppAID = null;// 用于切换默认卡

    public TsmCard() {

    }

    public String getActiveAID() {
        return mCurActAppAID;
    }

    public void setActiveAID(String aid) {
        if (aid.equalsIgnoreCase(mCurActAppAID)) {
            return;
        }
        mCurActAppAID = aid;
        if (!TsmBusinessConfig.isInWhiteList(aid)) {
            return;
        }
        updateAppletWhiteList();
    }

    private int isCurActAppid(String aid) {
        return (mCurActAppAID.equalsIgnoreCase(aid)) ? 1 : 0;
    }

    private void updateAppletWhiteList() {
        int size = mAppletWhiteList.length();
        for (int index = 0; index < size; index++) {
            try {
                JSONObject obj = mAppletWhiteList.getJSONObject(index);
                obj.put(Constants.TSM_KEY_APP_SELECT,
                        isCurActAppid(obj.optString(Constants.TSM_KEY_AID)));
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

    }

    public void setCPLC(String cplc) {
        mCPLC = cplc;
    }

    public String getCPLC() {
        return mCPLC;
    }

    public String getMainAID() {
        return mMainAID;
    }

    public void update(CardStatusContext context) {

        mMainAID = context.mainAID;
        // TODO
        mAppletList.addAll(context.appList);
        mSSDList.addAll(context.ssdList);
        for (AppletStatus tmp : mAppletList) {
            if (TsmBusinessConfig.isInWhiteList(tmp.AID)) {
                JSONObject obj = new JSONObject();
                try {
                    obj.put(Constants.TSM_KEY_AID, tmp.AID);
                    obj.put(Constants.TSM_KEY_APP_STAT, tmp.getStatus());
                    obj.put(Constants.TSM_KEY_APP_SELECT, isCurActAppid(tmp.AID));
                    mAppletWhiteList.put(obj);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public AppletStatus getAppletByAID(String aid) {
        AppletStatus target = null;
        for (AppletStatus status : mAppletList) {
            if (status.AID.equalsIgnoreCase(aid)) {
                target = status;
                break;
            }
        }
        return target;
    }

    public String getSSDIDByAppID(String aid) {
        AppletStatus status = getAppletByAID(aid);
        if (status == null) {
            return null;
        }
        String sdaid = status.sdAID;
        if (sdaid.equalsIgnoreCase(mMainAID)) {
            return null;
        }
        return sdaid;
    }

    public SSDStatus getSSDByAID(String aid) {
        SSDStatus targetSSD = null;
        for (SSDStatus baseStatus : mSSDList) {
            if (aid.equalsIgnoreCase(baseStatus.AID)) {
                targetSSD = baseStatus;
                break;
            }
        }
        return targetSSD;
    }

    public void clear() {
        mCPLC = null;
        mMainAID = null;
        mAppletList.clear();
        mAppletList = null;
        mSSDList.clear();
        mSSDList = null;
    }

    public JSONArray getAppletWhiteList() {
        return mAppletWhiteList;
    }

}
