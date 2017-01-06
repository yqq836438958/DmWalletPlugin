
package com.pacewear.tsm.common;

import android.text.TextUtils;

public class UniSessionStore {

    private volatile static UniSessionStore mInstacne = null;

    private final static Object oLocked = new Object();

    private volatile String mSessionId = "";

    private UniSessionStore() {
    }

    public static UniSessionStore getInstance() {

        if (mInstacne == null) {
            synchronized (oLocked) {
                if (mInstacne == null) {
                    mInstacne = new UniSessionStore();
                }
            }
        }
        return mInstacne;
    }

    public void update(String sessionId) {
        if (TextUtils.isEmpty(sessionId)) {
            return;
        }
        synchronized (oLocked) {
            mSessionId = sessionId;
        }
    }

    public String getId() {
        return mSessionId;
    }

    public void clear() {
        mSessionId = "";
    }

}
