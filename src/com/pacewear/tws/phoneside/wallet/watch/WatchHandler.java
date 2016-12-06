
package com.pacewear.tws.phoneside.wallet.watch;

import android.os.Handler;
import android.os.Message;
import android.support.v4.util.LongSparseArray;

import com.pacewear.tws.phoneside.wallet.common.Utils;
import com.qq.taf.jce.JceStruct;
import com.tencent.tws.framework.common.DevMgr;
import com.tencent.tws.framework.common.Device;
import com.tencent.tws.framework.common.MsgCmdDefine;
import com.tencent.tws.framework.common.MsgSender;
import com.tencent.tws.framework.common.MsgSender.MsgSendCallBack;
import com.tencent.tws.proto.wallet.WatchNFCManager.MSG;
import com.tencent.tws.proto.wallet.WatchNFCManager.MSG_STATE;

import qrom.component.log.QRomLog;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * @author baodingzhou
 */

public class WatchHandler implements IWatchHandler, IWatchHandlerListener, MsgSendCallBack {

    private static final String TAG = "WatchHandler";

    private static volatile WatchHandler sInstance = null;

    private final ArrayList<IWatchHandlerListener> mWatchHandlerListeners = new ArrayList<IWatchHandlerListener>();

    private Handler mHandler = null;

    private static final int MSG_HANDLE_MSG_FROM_WATCH = 3205;

    private static final int MSG_HANDLE_SEND_MSG_TO_WATCH = MSG_HANDLE_MSG_FROM_WATCH + 1;

    private static final int MSG_HANDLE_RE_SEND_MSG_TO_WATCH = MSG_HANDLE_SEND_MSG_TO_WATCH + 1;

    private static final int MSG_HANDLE_SEND_TO_WATCH_RESULT = MSG_HANDLE_RE_SEND_MSG_TO_WATCH + 1;

    /**
     * 发送缓存
     */
    private LongSparseArray<MSGHolder> mSendBuffer = new LongSparseArray<MSGHolder>();

    /**
     * ResultMsg
     */
    public static class MSGHolder {

        public static final int MAX_RETRY = 5;

        public static final int PER_RETRY_DELAY_MILLIS = 100;

        int mRetry = 0;

        private MSG mMSG = null;

        public MSGHolder(MSG msg) {
            mMSG = msg;
            mRetry = 0;
        }

        public boolean isTryAgain() {
            mRetry++;
            if (mRetry <= MAX_RETRY) {
                return true;
            }

            return false;
        }
    }

    /**
     * ResultMsg
     */
    public static class MsgResult {

        public MsgResult(long sendReqId, boolean succeed, int reason) {
            mSendReqId = sendReqId;
            mSucceed = succeed;
            mReason = reason;
        }

        long mSendReqId = 0;
        boolean mSucceed = false;
        int mReason = 0;
    }

    public static IWatchHandler getInstance() {

        if (sInstance == null) {
            synchronized (WatchHandler.class) {
                if (sInstance == null) {
                    sInstance = new WatchHandler();
                }
            }
        }

        return sInstance;
    }

    private WatchHandler() {
        mHandler = new Handler(Utils.getWorkerlooper()) {

            @Override
            public void handleMessage(Message message) {
                QRomLog.d(TAG, "handleMessage what: " + message.what);

                super.handleMessage(message);
                boolean handled = false;

                switch (message.what) {
                    case MSG_HANDLE_MSG_FROM_WATCH:
                        handled = onMsgReceived((MSG) message.obj);
                        break;

                    case MSG_HANDLE_SEND_MSG_TO_WATCH:
                        handled = handleSendMsgToWatch((MSG) message.obj);
                        break;

                    case MSG_HANDLE_RE_SEND_MSG_TO_WATCH:
                        handled = handleSendMsgToWatch((MSGHolder) message.obj);
                        break;

                    case MSG_HANDLE_SEND_TO_WATCH_RESULT:
                        handled = handleSendToWatchResult((MsgResult) message.obj);
                        break;
                }

                if (!handled) {
                    // impossible
                    QRomLog.e(TAG, "Impossible error for not handled what " + message.what);
                }
            }
        };
    }

    @Override
    public void registerWatchHandlerListener(IWatchHandlerListener listener) {
        QRomLog.d(TAG, "registerWatchHandlerListener");
        synchronized (mWatchHandlerListeners) {
            if (!mWatchHandlerListeners.contains(mWatchHandlerListeners)) {
                mWatchHandlerListeners.add(listener);
            }
        }
    }

    @Override
    public void unregisterWatchHandlerListener(IWatchHandlerListener listener) {
        QRomLog.d(TAG, "unregisterWatchHandlerListener");
        synchronized (mWatchHandlerListeners) {
            if (mWatchHandlerListeners.contains(listener)) {
                mWatchHandlerListeners.remove(listener);
            }
        }
    }

    private boolean sendResultMsg(MsgResult result) {
        Message message = new Message();
        message.what = MSG_HANDLE_SEND_TO_WATCH_RESULT;
        message.obj = result;
        return mHandler.sendMessage(message);
    }

    @Override
    public void onSendResult(boolean bSuc, long lSendReqId) {
        sendResultMsg(new MsgResult(lSendReqId, bSuc, 0));
    }

    @Override
    public void onLost(int nReason, long lSendReqId) {
        sendResultMsg(new MsgResult(lSendReqId, false, 0));
    }

    @Override
    public boolean sendMsgToWatch(MSG msg) {
        QRomLog.d(TAG, "sendMsgToWatch: " + JceStruct.toDisplaySimpleString(msg));

        Message message = new Message();
        message.what = MSG_HANDLE_SEND_MSG_TO_WATCH;
        message.obj = msg;
        return mHandler.sendMessage(message);
    }

    private boolean reSendMsgToWatch(MSGHolder msgHolder) {
        QRomLog.d(TAG, "reSendMsgToWatch");

        Message message = new Message();
        message.what = MSG_HANDLE_RE_SEND_MSG_TO_WATCH;
        message.obj = msgHolder;
        return mHandler.sendMessage(message);
    }

    private long send(MSG msg) {

        Device device = DevMgr.getInstance().connectedDev();
        if (device == null) {
            QRomLog.d(TAG, "Device not connected!");
            return -1;
        }

        return MsgSender.getInstance().sendCmd(device, MsgCmdDefine.CMD_NFC_WALLET_MSG_FROM_PHONE,
                msg, this);
    }

    private boolean handleSendMsgToWatch(MSG msg) {
        QRomLog.d(TAG, "handleSendMsgToWatch");
        return handleSendMsgToWatch(new MSGHolder(msg));
    }

    private boolean handleSendMsgToWatch(MSGHolder msgHolder) {
        QRomLog.d(TAG, "handleSendMsgToWatch");

        long sendReqId = send(msgHolder.mMSG);

        if (sendReqId >= 0) {
            mSendBuffer.put(sendReqId, msgHolder);
        } else {
            if (msgHolder.isTryAgain()) {
                reSendMsgToWatch(msgHolder);
            } else {
                // Notify send failed.
                msgHolder.mMSG.stMsgHeader.eState = MSG_STATE._UNREACH;
                onMsgReceived(msgHolder.mMSG);
            }
        }

        return true;
    }

    private boolean handleSendToWatchResult(MsgResult result) {
        QRomLog.d(TAG, "handleSendToWatchResult");

        MSGHolder msgHolder = mSendBuffer.get(result.mSendReqId);

        if (msgHolder == null) {
            QRomLog.e(TAG, "Can not get msgHolder for " + result.mSendReqId);
            return false;
        }

        mSendBuffer.remove(result.mSendReqId);

        if (!result.mSucceed) {
            if (msgHolder.isTryAgain()) {
                reSendMsgToWatch(msgHolder);
            } else {
                // notify send failed.
                msgHolder.mMSG.stMsgHeader.eState = MSG_STATE._UNREACH;
                onMsgReceived(msgHolder.mMSG);
            }
        } else {
            // Do nothing.
        }

        return true;
    }

    @Override
    public boolean dispatchMsgFromWatch(MSG msg) {
        QRomLog.d(TAG, "handleMsgFromWatch: " + JceStruct.toDisplaySimpleString(msg));

        Message message = new Message();
        message.what = MSG_HANDLE_MSG_FROM_WATCH;
        message.obj = msg;
        return mHandler.sendMessage(message);
    }

    @Override
    public boolean onMsgReceived(MSG msg) {
        QRomLog.d(TAG, "onMsgReceived");

        boolean handled = false;
        Iterator<IWatchHandlerListener> iterator = null;
        IWatchHandlerListener listener = null;

        synchronized (mWatchHandlerListeners) {
            iterator = mWatchHandlerListeners.iterator();

            if (iterator != null) {
                while (iterator.hasNext()) {
                    listener = iterator.next();
                    handled |= listener.onMsgReceived(msg);
                }
            }
        }

        QRomLog.d(TAG, "final handled:" + handled);

        return handled;
    }
}
