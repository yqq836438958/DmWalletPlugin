
package com.pacewear.tsm.internal;

import com.pacewear.httpserver.IResponseObserver;
import com.pacewear.tsm.ITsmBusinessListener;
import com.pacewear.tsm.business.TsmBaseBusiness.IBusinessInterceptListener;
import com.pacewear.tsm.card.TsmCard;
import com.pacewear.tsm.card.TsmContext;
import com.pacewear.tsm.channel.ITsmAPDUCallback;
import com.pacewear.tsm.channel.ITsmCardChannel;
import com.pacewear.tsm.server.tosservice.ReportAPDUResult;
import com.pacewear.tsm.server.tosservice.TSMTosService;
import com.qq.taf.jce.JceStruct;

import java.util.ArrayList;
import java.util.List;

public abstract class TsmBaseProcess implements ITsmProcess {
    protected enum PROCESS_STATUS {
        IDLE, WORKING, REPEAT, KEEP, FINISH,
    }

    protected final int CHECK_READY = 0;
    protected final int CHECK_SKIP = 1;
    protected final int CHECK_ERROR = 2;
    private int mReportType = 0;
    private ITsmBusinessListener mBusinessListener = null;
    private ITsmProcess mNextProcessor = null;
    private ITsmCardChannel mCardChannel = null;
    protected TsmContext mContext = null;
    protected TsmCard mTsmCard = null;
    protected IBusinessInterceptListener mInterceptCallback = null;

    public TsmBaseProcess(TsmContext context, int reportType, IBusinessInterceptListener callback) {
        this(context, reportType);
        mInterceptCallback = callback;
    }

    public TsmBaseProcess(TsmContext context, int reportType) {
        mReportType = reportType;
        mContext = context;
        mBusinessListener = mContext.getITsmBusinessListener();
        mCardChannel = mContext.getChannel();
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

    @Override
    public boolean start() {
        int checkRet = onCheck();
        if (checkRet == CHECK_READY) {
            return onStart();
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

    protected abstract int getApduList(JceStruct rsp, List<String> apdus, boolean fromLoacal);

    protected void setProcessStatus(PROCESS_STATUS stat) {
        mCurrentComStatus = stat;
        if (mCurrentComStatus == PROCESS_STATUS.FINISH) {
            finishProcess();
        } else if (mCurrentComStatus == PROCESS_STATUS.REPEAT) {
            repeatProcess();
        }
    }

    protected final boolean process(TSMTosService service, final OnTsmProcessCallback callback) {
        List<String> localList = new ArrayList<String>();
        getApduList(null, localList, true);
        // 首先判断是否有可用的缓存apdu指令，如果有，那么直接发送指令即可
        if (localList != null && localList.size() > 0) {
            return onTransmit(localList, callback);
        }
        if (service == null) {
            return false;
        }
        final long uniqReq = service.getUniqueSeq();
        boolean handle = service.invoke(new IResponseObserver() {

            @Override
            public void onResponseSucceed(long uniqueSeq, int operType, JceStruct response) {
                if (uniqReq == uniqueSeq) {
                    List<String> apdulist = new ArrayList<String>();
                    int iRet = getApduList(response, apdulist, false);
                    if (iRet != 0) {
                        onHandleFail(callback, -1, null);
                        return;
                    }
                    // 若任务无需再发apdu指令，那么在此进行结束
                    if (returnWithoutTransmit()) {
                        onHandleSuccess(callback, null);
                        return;
                    }
                    if (apdulist == null || apdulist.size() <= 0) {
                        onHandleFail(callback, -1, null);
                        return;
                    }
                    onTransmit(apdulist, callback);
                }
            }

            @Override
            public void onResponseFailed(long uniqueSeq, int operType, int errorCode,
                    String description) {
                if (uniqReq == uniqueSeq) {
                    onHandleFail(callback, errorCode, description);
                }
            }
        });
        return handle;
    }

    private boolean onTransmit(final List<String> apduList,
            final OnTsmProcessCallback callback) {
        if (mCardChannel == null) {
            return false;
        }
        return mCardChannel.transmit(apduList, new ITsmAPDUCallback() {

            @Override
            public void onSuccess(String[] apdus) {
                onHandleSuccess(callback, apdus);
            }

            @Override
            public void onFail(int error, String desc) {
                onHandleFail(callback, error, desc);
            }
        });
    }

    private void closeChannel() {
        if (mCardChannel != null) {
            mCardChannel.close();
        }
    }

    private void onHandleSuccess(OnTsmProcessCallback callback, String[] apdus) {
        if (callback != null) {
            callback.onSuccess(apdus);
        }
        // reportServer(0);
    }

    private void onHandleFail(OnTsmProcessCallback callback, int err, String desc) {
        if (callback != null) {
            callback.onFail(err, desc);
        }
        postHandleFail(err, desc);
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

    protected ITsmCardChannel getChannel() {
        return mCardChannel;
    }

    protected void reportRet2Server(int key, int status, String aid) {
        ReportAPDUResult report = new ReportAPDUResult(mContext);
        report.setParams(key, status, aid);
        report.invoke(null);// TODO 是否需要等待上报结果？？
    }

    protected boolean returnWithoutTransmit() {
        return false;
    }

    protected String onResult() {
        return "";
    }

    protected void onStop() {
        // do some clean job if Need
    }
}
