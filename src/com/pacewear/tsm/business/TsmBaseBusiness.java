
package com.pacewear.tsm.business;

import com.pacewear.tsm.ITsmBusinessListener;
import com.pacewear.tsm.business.TsmBusinessEnv.OnBusinessEnvCallback;
import com.pacewear.tsm.card.TsmContext;
import com.pacewear.tsm.internal.core.ITsmProcess;

public abstract class TsmBaseBusiness {
    public static interface IBusinessInterceptListener {
        public void onIntercept(int error, String desc);
    }

    protected TsmContext mContext = null;
    private TsmProcessList mProcessList;
    public static final int ENV_CHECK_DEFAULT = 0;
    public static final int ENV_CHECK_SKIP = 1;
    public static final int ENV_CHECK_FORCE = 2;
    protected int mEnvCheck = ENV_CHECK_DEFAULT;

    final class TsmProcessList {
        ITsmProcess head = null;
        ITsmProcess curProcess = null;
        int curPos = 0;

        TsmProcessList(ITsmProcess process) {
            head = process;
            curProcess = process;
            curPos++;
        }

        void add(ITsmProcess process) {
            curProcess.setNext(process);
            curProcess = process;
            curPos++;
        }
    }

    protected void checkEnv(int check) {
        mEnvCheck = check;
    }

    protected void addProcess(ITsmProcess process) {
        if (mProcessList == null) {
            mProcessList = new TsmProcessList(process);
        } else {
            mProcessList.add(process);
        }
    }

    public TsmBaseBusiness(TsmContext ctx) {
        mContext = ctx;
    }

    public boolean start() {
        mContext.initIfNeed();
        if (mEnvCheck == ENV_CHECK_SKIP) {
            beginBusiness();
            return true;
        }
        TsmBusinessEnv env = new TsmBusinessEnv(mContext, mEnvCheck == ENV_CHECK_FORCE);
        boolean handle = env.setup(new OnBusinessEnvCallback() {

            @Override
            public void onSucess() {
                beginBusiness();
            }

            @Override
            public void onFail(int ret, String desc) {
                onFailImpl(ret, desc);
            }
        });
        return handle;
    }

    private boolean startNow() {
        if (mProcessList == null || mProcessList.head == null) {
            return false;
        }
        return mProcessList.head.start();
    }

    private void beginBusiness() {
        boolean hanlde = onStart();
        if (hanlde) {
            startNow();
        }
    }

    private void onFailImpl(int err, String desc) {
        ITsmBusinessListener callback = mContext.getITsmBusinessListener();
        if (callback != null) {
            callback.onFail(err, desc);
        }
    }

    protected abstract boolean onStart();

}
