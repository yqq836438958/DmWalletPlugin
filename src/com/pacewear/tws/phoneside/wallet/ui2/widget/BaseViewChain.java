
package com.pacewear.tws.phoneside.wallet.ui2.widget;

import java.util.ArrayList;
import java.util.List;

public class BaseViewChain {
    private List<BaseViewHandler> mList = new ArrayList<BaseViewHandler>();
    private int mSize = 0;

    public static abstract class BaseViewHandler {

        private BaseViewHandler mNext = null;

        void setNext(BaseViewHandler next) {
            mNext = next;
        }

        void invoke() {
            if (isConditionReady()) {
                onHandle();
            } else if (mNext != null) {
                mNext.invoke();
            }
        }

        public abstract boolean isConditionReady();

        public abstract void onHandle();
    }

    void add(BaseViewHandler node) {
        if (node == null) {
            return;
        }
        synchronized (BaseViewHandler.class) {
            if (mSize > 0) {
                mList.get(mSize - 1).setNext(node);
            }
            mList.add(node);
            mSize++;
        }
    }

    final void invoke() {
        if (mList.size() <= 0) {
            return;
        }
        mList.get(0).invoke();
    }

}
