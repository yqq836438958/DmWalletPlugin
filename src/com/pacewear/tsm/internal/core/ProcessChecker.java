
package com.pacewear.tsm.internal.core;

import android.text.TextUtils;

import com.pacewear.tsm.card.TsmContext;
import com.pacewear.tsm.common.UniSessionStore;

public class ProcessChecker implements IProcessChecker {
    private boolean mNeedSession = false;
    private String mCheckAid = null;
    private TsmContext mTsmContext = null;

    public ProcessChecker(TsmContext context, String aid, boolean needSession) {
        mTsmContext = context;
        mCheckAid = aid;
        mNeedSession = needSession;
    }

    private boolean isAidPresent() {
        String aid = mTsmContext.getCard().getFocusAID();
        if (mCheckAid.equalsIgnoreCase(aid)) {
            return true;
        }
        return false;
    }

    private boolean isSessionReady() {
        if (mNeedSession) {
            return !TextUtils.isEmpty(UniSessionStore.getInstance().getId());
        }
        return true;
    }

    @Override
    public boolean invoke(OnTsmProcessCallback callback) {
        TsmOpenSession session = new TsmOpenSession(mTsmContext, mCheckAid, mNeedSession);
        return session.invoke(callback);
    }

    @Override
    public boolean isReady() {
        return isAidPresent() && isSessionReady();
    }

}
