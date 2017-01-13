
package com.pacewear.tsm.internal.core;

import com.pacewear.tsm.ITsmBusinessListener;
import com.pacewear.tsm.business.TsmBaseBusiness.IBusinessInterceptListener;
import com.pacewear.tsm.card.TsmCard;
import com.pacewear.tsm.card.TsmContext;
import com.pacewear.tsm.channel.ITsmCardChannel;
import com.pacewear.tsm.server.tosservice.ReportAPDUResult;
import com.pacewear.tsm.server.tosservice.TSMTosService;
import com.qq.taf.jce.JceStruct;

import java.util.List;

public abstract class TsmBaseProcess implements ITsmProcess, IProcessEventConsum {
    protected enum PROCESS_STATUS {
        IDLE, WORKING, REPEAT, KEEP, FINISH,
    }

    protected final int CHECK_READY = 0;
    protected final int CHECK_SKIP = 1;
    protected final int CHECK_ERROR = 2;
    private ITsmBusinessListener mBusinessListener = null;
    private ITsmProcess mNextProcessor = null;
    protected TsmContext mContext = null;
    protected TsmCard mTsmCard = null;
    protected IBusinessInterceptListener mInterceptCallback = null;
    protected String mContainerAID = null;
    private IProcessChecker mProcessChecker = null;
    private IApduTransmiter mApduTransmiter = null;

    // public TsmBaseProcess(TsmContext context, String aid, boolean needSession,
    // IBusinessInterceptListener callback) {
    // this(context, aid, needSession);
    // mInterceptCallback = callback;
    // }

    public TsmBaseProcess(TsmContext context, String aid, boolean needSession) {
        mProcessChecker = new ProcessChecker(context, aid, needSession);
        mApduTransmiter = new ApduTransmiter(context.getChannel(), this);
        mContext = context;
        mContainerAID = aid;
        mBusinessListener = mContext.getITsmBusinessListener();
        mTsmCard = mContext.getCard();
    }

    protected PROCESS_STATUS mCurrentComStatus = PROCESS_STATUS.IDLE;

    @Override
    public boolean isFinish() {
        return mCurrentComStatus == PROCESS_STATUS.FINISH;
    }

    @Override
    public boolean isIdle() {
        return mCurrentComStatus == PROCESS_STATUS.IDLE;
    }

    @Override
    public boolean setNext(ITsmProcess nextHanlder) {
        mNextProcessor = nextHanlder;
        return true;
    }

    private boolean startProcessInternal() {
        if (mProcessChecker.isReady()) {
            return onStart();
        }
        OnTsmProcessCallback callback = new OnTsmProcessCallback() {

            @Override
            public void onSuccess(String[] apduList) {
                onStart();
            }

            @Override
            public void onFail(int error, String desc) {
                postHandleFail(error, desc);
            }
        };
        return mProcessChecker.invoke(callback);
    }

    @Override
    public boolean start() {
        int checkRet = onCheck();
        if (checkRet == CHECK_READY) {
            return startProcessInternal();
        }
        if (checkRet == CHECK_SKIP) {
            finishProcess();
            return true;
        }
        postHandleFail(checkRet, null);
        return false;
    }

    protected abstract int onCheck();

    protected abstract boolean onStart();

    protected abstract int onParse(JceStruct rsp, List<String> apdus, boolean fromLoacal);

    protected void setProcessStatus(PROCESS_STATUS stat) {
        mCurrentComStatus = stat;
        if (mCurrentComStatus == PROCESS_STATUS.FINISH) {
            finishProcess();
        } else if (mCurrentComStatus == PROCESS_STATUS.REPEAT) {
            repeatProcess();
        }
    }

    protected final boolean process(TSMTosService service, final OnTsmProcessCallback callback) {
        return mApduTransmiter.transmit(service, callback);
    }

    private void closeChannel() {
        mApduTransmiter.close();
    }

    protected void postHandleFail(int error, String desc) {
        if (mInterceptCallback != null) {
            mInterceptCallback.onIntercept(error, desc);
            return;
        }
        if (mBusinessListener != null) {
            mBusinessListener.onFail(error, desc);
        }
        closeChannel();
        // reportServer(err);
    }

    private void repeatProcess() {
        onStart();
    }

    protected void finishProcess() {
        onStop();
        if (mNextProcessor != null) {
            mNextProcessor.start();
        } else {
            // 无接手者，认为任务已经结束
            if (mBusinessListener != null) {
                mBusinessListener.onSuccess(onResult());
            }
            closeChannel();
        }
    }

    protected void reportRet2Server(int key, int status, String aid) {
        ReportAPDUResult report = new ReportAPDUResult(mContext);
        report.setParams(key, status, aid);
        report.invoke(null);// TODO 是否需要等待上报结果？？
    }

    @Override
    public boolean returnWithoutTransmit() {
        return canStopWithoutTransmit();
    }

    protected boolean canStopWithoutTransmit() {
        return false;
    }

    protected String onResult() {
        return "";
    }

    protected void onStop() {
        // do some clean job if Need
    }

    @Override
    public int onParserApdu(JceStruct rsp, List<String> apdus, boolean fromLocal) {
        return onParse(rsp, apdus, fromLocal);
    }
}
