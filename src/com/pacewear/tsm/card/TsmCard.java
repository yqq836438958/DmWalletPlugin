
package com.pacewear.tsm.card;

import android.text.TextUtils;

import com.pacewear.tsm.business.TsmBusinessConfig;
import com.pacewear.tsm.common.APDUUtil;
import com.pacewear.tsm.common.CacheUtil;
import com.pacewear.tsm.common.Constants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import TRom.AppletStatus;
import TRom.CardStatusContext;
import TRom.E_APP_LIFE_STATUS;
import TRom.SSDStatus;

public class TsmCard {
    private String mCPLC = null;
    private String mMainAID = null;
    private List<AppletStatus> mRemoteAppletList = new ArrayList<AppletStatus>();
    private List<SSDStatus> mRemoteSSDList = new ArrayList<SSDStatus>();
    private JSONArray mOutPutCardList = null;
    private String mFocusAID = null;
    private List<CardListItem> mCardListItems = new ArrayList<CardListItem>();
    private final String KEY_CPLC = "key_cplc_";

    public TsmCard() {
        mFocusAID = mMainAID;
    }

    public void syncLocalCache() {
        if (mOutPutCardList != null && mOutPutCardList.length() > 0) {
            return;
        }
        String cacheISD = CacheUtil.getCacheISD();
        String cacheCPLC = CacheUtil.getCacheCPLC();
        mMainAID = TextUtils.isEmpty(cacheISD) ? Constants.TSM_DEFAULT_CARDMAIN_AID : cacheISD;
        mCPLC = TextUtils.isEmpty(cacheCPLC) ? "" : cacheCPLC;
        if (!TextUtils.isEmpty(mCPLC)) {
            String tmpCardList = CacheUtil.get(KEY_CPLC + mCPLC, "");
            try {
                mOutPutCardList = new JSONArray(tmpCardList);
            } catch (JSONException e) {
                mOutPutCardList = null;
                e.printStackTrace();
            }
            if (mOutPutCardList != null) {
                unwrapCardList();
            }
        }
    }

    private void unwrapCardList() {
        JSONObject jsonObject = null;
        for (int index = 0; index < mOutPutCardList.length(); index++) {
            jsonObject = mOutPutCardList.optJSONObject(index);
            String aid = jsonObject.optString(Constants.TSM_KEY_AID);
            int iInstallStat = jsonObject.optInt(Constants.TSM_KEY_APP_STAT);
            boolean bActive = jsonObject.optBoolean(Constants.TSM_KEY_APP_SELECT);
            mCardListItems.add(new CardListItem(aid, iInstallStat, bActive));
        }
    }

    public String getFocusAID() {
        return mFocusAID;
    }

    public void setFocusAID(String aid) {
        mFocusAID = aid;
    }

    public boolean isAIDActive(String aid) {
        boolean isActivte = false;
        for (CardListItem item : mCardListItems) {
            if (item.aid.equals(aid)) {
                isActivte = item.bActive;
                break;
            }
        }
        return isActivte;
    }

    public void updateAllActiveStatus(String rspAPDU) {
        int iStat = -1;
        for (CardListItem item : mCardListItems) {
            iStat = APDUUtil.parseAppStat(rspAPDU, item.aid);
            if (iStat == 0) {
                item.bActive = false;
            } else if (iStat == 1) {
                item.bActive = true;
            }
        }
        flushOutCardListQuery();
    }

    public void unactiveAID(String aid) {
        updateCardListItemActiveStat(aid, false);
    }

    public void activeAID(String aid) {
        updateCardListItemActiveStat(aid, true);
    }

    public void updateCardListItemInstallStat(String aid, int install) {
        for (CardListItem item : mCardListItems) {
            if (item.aid.equals(aid)) {
                item.iInstallStat = install;
                break;
            }
        }
        flushOutCardListQuery();
    }

    private void updateCardListItemActiveStat(String aid, boolean active) {
        for (CardListItem item : mCardListItems) {
            if (item.aid.equals(aid)) {
                item.bActive = active;
                break;
            }
        }
        flushOutCardListQuery();
    }

    private void flushOutCardListQuery() {
        if (mOutPutCardList == null) {
            mOutPutCardList = new JSONArray();
        }
        for (CardListItem item : mCardListItems) {
            JSONObject obj = new JSONObject();
            try {
                obj.put(Constants.TSM_KEY_AID, item.aid);
                obj.put(Constants.TSM_KEY_APP_STAT, genInstallStatus(item.iInstallStat));
                obj.put(Constants.TSM_KEY_APP_SELECT, genActiveStatus(item.bActive));
                mOutPutCardList.put(obj);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        CacheUtil.save(KEY_CPLC + mCPLC, mOutPutCardList.toString());
    }

    public String getOutputCardlist() {
        return (mOutPutCardList != null && mOutPutCardList.length() > 0)
                ? mOutPutCardList.toString() : "";
    }

    public List<CardListItem> getExistCardList() {
        return mCardListItems;
    }

    private int genInstallStatus(int iInstallStat) {
        int stat = 0;
        switch (iInstallStat) {
            case -1:

                break;

            default:
                break;
        }
        return stat;
    }

    private int genActiveStatus(boolean bActiveStat) {
        return bActiveStat ? 1 : 0;
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

    public void syncRemote(CardStatusContext context) {

        mMainAID = context.mainAID;
        // TODO
        mRemoteAppletList.addAll(context.appList);
        mRemoteSSDList.addAll(context.ssdList);
        mCardListItems.clear();
        int installStat = Constants.TSM_APP_UNINSTALL;
        for (AppletStatus tmp : mRemoteAppletList) {
            if (TsmBusinessConfig.isInWhiteList(tmp.AID)) {
                if (tmp.status == E_APP_LIFE_STATUS._EALS_PERSONALIZED) {
                    installStat = Constants.TSM_APP_PERSONAL;
                } else if (tmp.status == E_APP_LIFE_STATUS._EALS_INSTALL_FOR_MAKESELECT) {
                    installStat = Constants.TSM_APP_INSTALL;
                }
                CardListItem item = new CardListItem(tmp.AID, installStat);
                mCardListItems.add(item);
            }
        }
        flushOutCardListQuery();
    }

    public AppletStatus getAppletByAID(String aid) {
        AppletStatus target = null;
        for (AppletStatus status : mRemoteAppletList) {
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
        for (SSDStatus baseStatus : mRemoteSSDList) {
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
        mRemoteAppletList.clear();
        mRemoteAppletList = null;
        mRemoteSSDList.clear();
        mRemoteSSDList = null;
    }
}
