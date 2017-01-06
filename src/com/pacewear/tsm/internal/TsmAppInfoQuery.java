
package com.pacewear.tsm.internal;

import com.pacewear.tsm.TsmConstants;
import com.pacewear.tsm.card.TsmContext;
import com.pacewear.tsm.common.ByteUtil;
import com.pacewear.tsm.query.ITsmApplet;
import com.pacewear.tsm.query.TsmApplet;
import com.pacewear.tsm.query.TsmApplet.AppletTagQuery;
import com.pacewear.tsm.server.tosservice.AppInfoQuery;
import com.qq.taf.jce.JceStruct;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import TRom.AppInfoQueryRsp;
import TRom.TagQuery;

public class TsmAppInfoQuery extends TsmBaseProcess {
    private String mAppAid = null;
    private JSONObject mQueryResult = null;
    private AppletTagQuery mTagQuery = null;
    private List<String> mToFilterTagList = null;
    private int mQueryIndex = -1;

    public TsmAppInfoQuery(TsmContext context, String appletAID, List<String> tagList) {
        super(context, TsmConstants.TSM_TYPE_APPINFO);
        mAppAid = appletAID;
        mToFilterTagList = tagList;
        mQueryIndex = 0;
        init();
    }

    @Override
    protected int onCheck() {
        return CHECK_READY;
    }

    private void init() {
        Object tmpObj = ByteUtil.getClassObjectFormCache(mAppAid);
        if (tmpObj != null) {
            mTagQuery = (AppletTagQuery) tmpObj;
        }
        mQueryResult = new JSONObject();
    }

    @Override
    protected boolean onStart() {
        setProcessStatus(PROCESS_STATUS.WORKING);
        AppInfoQuery query = null;
        if (mTagQuery == null) {
            query = new AppInfoQuery(mContext, mAppAid);
        }
        process(query, new OnTsmProcessCallback() {

            @Override
            public void onSuccess(String[] apduList) {
                parseQueryResult(apduList);
                if (mQueryIndex >= mToFilterTagList.size()) {
                    setProcessStatus(PROCESS_STATUS.FINISH);
                } else {
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
    protected int getApduList(JceStruct rsp, List<String> apdus, boolean fromLoacal) {
        List<String> tmpList = getCurTransmitApdus();
        if (fromLoacal && tmpList != null && tmpList.size() > 0) {
            apdus.addAll(tmpList);
            return 0;
        }
        AppInfoQueryRsp data = (AppInfoQueryRsp) rsp;
        if (data.iRet != 0) {
            return data.iRet;
        }
        if (data.vTagList == null || data.vTagList.size() <= 0) {
            return -1;
        }
        mTagQuery = new AppletTagQuery(mAppAid);
        for (TagQuery tagQuery : data.vTagList) {
            mTagQuery.put(tagQuery.sTag, tagQuery.sAPDU);
        }
        ByteUtil.saveClassObject2Cache(mAppAid, mTagQuery);
        apdus.addAll(getCurTransmitApdus());
        return 0;
    }

    private void parseQueryResult(String[] apduList) {
        ITsmApplet applet = TsmApplet.get(mAppAid);
        String tag = mToFilterTagList.get(mQueryIndex);
        String tmp = applet.parse(tag, apduList);
        try {
            mQueryResult.put(tag, tmp);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mQueryIndex++;
    }

    @Override
    protected String onResult() {
        return mQueryResult.toString();
    }

    private List<String> getCurTransmitApdus() {
        if (mTagQuery == null) {
            return null;
        }
        String tag = mToFilterTagList.get(mQueryIndex);
        return mTagQuery.getApdusByTag(tag);
    }
}
