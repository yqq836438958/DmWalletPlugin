
package com.pacewear.tws.phoneside.wallet.rpc;

import android.os.Handler;
import android.os.Message;
import android.support.v4.util.LongSparseArray;

import com.pacewear.tws.phoneside.wallet.common.Constants;
import com.pacewear.tws.phoneside.wallet.common.Utils;
import com.pacewear.tws.phoneside.wallet.watch.IWatchHandler;
import com.pacewear.tws.phoneside.wallet.watch.IWatchHandlerListener;
import com.pacewear.tws.phoneside.wallet.watch.WatchHandler;
import com.tencent.tws.proto.wallet.WatchNFCManager.MSG;
import com.tencent.tws.proto.wallet.WatchNFCManager.MSG_HEADER;
import com.tencent.tws.proto.wallet.WatchNFCManager.MSG_ORIENTATION;
import com.tencent.tws.proto.wallet.WatchNFCManager.MSG_RPC;
import com.tencent.tws.proto.wallet.WatchNFCManager.MSG_RPC_PARAMS;
import com.tencent.tws.proto.wallet.WatchNFCManager.MSG_STATE;
import com.tencent.tws.proto.wallet.WatchNFCManager.MSG_TYPE;

import qrom.component.log.QRomLog;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * @author baodingzhou
 */

public class RPCHandler implements IRPCHandler, IWatchHandlerListener, IRPCHandlerListener {

    private static final String TAG = "RPCHandler";

    private static final long MIN_RPC_TIMEOUT_MILLIS = 4000;

    private static volatile IRPCHandler sInstance = null;

    private IWatchHandler mWatchHandler = null;

    private Handler mHandler = null;

    private ArrayList<IRPCHandlerListener> mRPCHandlerListeners = new ArrayList<IRPCHandlerListener>();

    private LongSparseArray<MSG> mMsgSended = new LongSparseArray<MSG>();

    private final Object oCheckTimeOutLock = new Object();

    private static final int MSG_SEND_RPC_MSG_TO_WATCH = 2561;

    private static final int MSG_HANDLE_RPC_MSG_FROM_WATCH = MSG_SEND_RPC_MSG_TO_WATCH + 1;

    private static final int MSG_CHECK_TIMEOUT_RPC = MSG_HANDLE_RPC_MSG_FROM_WATCH + 1;

    private static final int MSG_HANDLE_TIMEOUT_RPC = MSG_CHECK_TIMEOUT_RPC + 1;

    public static IRPCHandler getInstance() {
        if (sInstance == null) {
            synchronized (RPCHandler.class) {
                if (sInstance == null) {
                    sInstance = new RPCHandler();
                }
            }
        }

        return sInstance;
    }

    private RPCHandler() {
        mWatchHandler = WatchHandler.getInstance();
        mWatchHandler.registerWatchHandlerListener(this);

        mHandler = new Handler(Utils.getWorkerlooper()) {

            @Override
            public void handleMessage(Message message) {
                super.handleMessage(message);

                QRomLog.d(TAG, "handleMessage waht " + message.what);

                switch (message.what) {
                    case MSG_SEND_RPC_MSG_TO_WATCH:
                        sendRPCMsgToWatch((MSG) message.obj);
                        break;
                    case MSG_HANDLE_RPC_MSG_FROM_WATCH:
                        handleRPCMsgFromWatch((MSG) message.obj);
                        break;
                    case MSG_CHECK_TIMEOUT_RPC:
                        autoCheckRPCTimeout((MSG) message.obj);
                        break;
                    case MSG_HANDLE_TIMEOUT_RPC:
                        handleTimeOutMsg((MSG) message.obj);
                        break;
                }
            }

        };
    }

    @Override
    public boolean onResult(long seq, MSG_RPC_PARAMS result) {
        boolean handled = false;
        IRPCHandlerListener listener = null;
        Iterator<IRPCHandlerListener> iterator = null;

        synchronized (mRPCHandlerListeners) {
            iterator = mRPCHandlerListeners.iterator();

            if (iterator != null) {
                while (iterator.hasNext()) {
                    listener = iterator.next();
                    handled = listener.onResult(seq, result);
                    if (handled) {
                        break;
                    }
                }
            }

            if (handled) {
                mRPCHandlerListeners.remove(listener);
            }

        }

        return handled;
    }

    @Override
    public boolean onExecption(long seq, MSG msg) {
        boolean handled = false;
        IRPCHandlerListener listener = null;
        Iterator<IRPCHandlerListener> iterator = null;

        synchronized (mRPCHandlerListeners) {
            iterator = mRPCHandlerListeners.iterator();

            if (iterator != null) {
                while (iterator.hasNext()) {
                    listener = iterator.next();
                    handled = listener.onExecption(seq, msg);
                    if (handled) {
                        break;
                    }
                }
            }

            if (handled) {
                mRPCHandlerListeners.remove(listener);
            }
        }

        return handled;
    }

    @Override
    public boolean onMsgReceived(MSG msg) {
        QRomLog.d(TAG, "onMsgReceived");
        boolean handled = false;

        if (!isMsgWilling(msg)) {
            return handled;
        }

        Message message = new Message();
        message.what = MSG_HANDLE_RPC_MSG_FROM_WATCH;
        message.obj = msg;
        return mHandler.sendMessage(message);
    }

    private boolean handleRPCMsgFromWatch(MSG msg) {
        boolean handled = false;
        MSG_RPC rcp = new MSG_RPC();
        rcp.readFrom(Utils.getJceInputStream(msg.vByteBody));
        if (msg.stMsgHeader.eState == MSG_STATE._SUCCEED) {
            handled = onResult(msg.stMsgHeader.lId, rcp.stOutParams);
        } else {
            handled = onExecption(msg.stMsgHeader.lId, msg);
        }
        return handled;
    }

    @Override
    public boolean registerIRPCHandlerListener(IRPCHandlerListener listener) {
        synchronized (mRPCHandlerListeners) {
            if (!mRPCHandlerListeners.contains(listener)) {
                mRPCHandlerListeners.add(listener);
            }
        }
        return true;
    }

    @Override
    public boolean unregisterIRPCHandlerListener(IRPCHandlerListener listener) {
        synchronized (mRPCHandlerListeners) {
            if (mRPCHandlerListeners.contains(listener)) {
                mRPCHandlerListeners.remove(listener);
            }
        }
        return true;
    }

    @Override
    public boolean sendRPCMsg(long seqID, MSG_RPC rpc, long timeoutMills) {
        QRomLog.d(TAG, "sendRPCMsg");

        MSG_HEADER header = new MSG_HEADER();
        header.lId = seqID;
        header.lTimeoutMillis = timeoutMills;
        header.stOrientation = MSG_ORIENTATION._FORWARD;
        header.eState = MSG_STATE._UNKNOWN;
        header.stType = MSG_TYPE._TYPE_RPC;
        if (header.lTimeoutMillis < MIN_RPC_TIMEOUT_MILLIS) {
            header.lTimeoutMillis = MIN_RPC_TIMEOUT_MILLIS;
        }
        header.lTimeoutAtMillis = System.currentTimeMillis() + header.lTimeoutMillis;

        MSG msg = new MSG();
        msg.stMsgHeader = header;
        msg.vByteBody = rpc.toByteArray(Constants.UTF8);

        Message message = new Message();
        message.what = MSG_SEND_RPC_MSG_TO_WATCH;
        message.obj = msg;

        return mHandler.sendMessage(message);
    }

    private boolean sendRPCMsgToWatch(MSG msg) {

        autoCheckRPCTimeout(msg);
        mWatchHandler.sendMsgToWatch(msg);

        return true;
    }

    private boolean autoCheckRPCTimeout(MSG msg) {
        QRomLog.d(TAG, "autoCheckRPCTimeout " + (msg != null));

        mHandler.removeMessages(MSG_CHECK_TIMEOUT_RPC);

        if (msg != null) {
            synchronized (oCheckTimeOutLock) {
                mMsgSended.put(msg.stMsgHeader.lId, msg);
            }
        }

        ArrayList<Long> timeOutMsgs = new ArrayList<Long>();
        long currentMillis = System.currentTimeMillis();
        long nextLatestMillis = Long.MAX_VALUE;
        MSG tmp = null;

        synchronized (oCheckTimeOutLock) {
            for (int i = 0; i < mMsgSended.size(); i++) {
                tmp = mMsgSended.valueAt(i);
                if (tmp.stMsgHeader.lTimeoutAtMillis <= currentMillis) {
                    timeOutMsgs.add(tmp.stMsgHeader.lId);
                } else if (tmp.stMsgHeader.lTimeoutAtMillis < nextLatestMillis) {
                    nextLatestMillis = tmp.stMsgHeader.lTimeoutAtMillis;
                }
            }
        }

        processTimeoutMsg(timeOutMsgs);

        if (nextLatestMillis < Long.MAX_VALUE) {
            QRomLog.d(TAG, "nextLatestMillis:" + nextLatestMillis);
            mHandler.sendEmptyMessageDelayed(MSG_CHECK_TIMEOUT_RPC, nextLatestMillis
                    - currentMillis);
        } else {
            QRomLog.d(TAG, "Not msg and do not need to check timeout");
        }

        return true;
    }

    private boolean processTimeoutMsg(ArrayList<Long> timeOutMsgs) {
        QRomLog.d(TAG, "processTimeoutMsg");

        if (timeOutMsgs == null || timeOutMsgs.size() <= 0) {
            return false;
        }

        Iterator<Long> iterator = timeOutMsgs.iterator();
        long seqId = 0;
        if (iterator != null) {
            MSG msg = null;
            while (iterator.hasNext()) {
                seqId = iterator.next();
                synchronized (oCheckTimeOutLock) {
                    msg = mMsgSended.get(seqId);
                    if (msg != null) {
                        mMsgSended.remove(seqId);
                    }
                }

                if (msg != null) {
                    sendMsgToHandleTimeoutRPC(msg);
                }
            }
        }

        return true;
    }

    private boolean sendMsgToHandleTimeoutRPC(MSG msg) {
        QRomLog.d(TAG, "sendMsgToHandleTimeoutRPC");
        Message message = new Message();
        message.what = MSG_HANDLE_TIMEOUT_RPC;
        message.obj = msg;
        return mHandler.sendMessage(message);
    }

    private boolean handleTimeOutMsg(MSG msg) {
        msg.stMsgHeader.eState = MSG_STATE._TIMEOUT;
        return handleRPCMsgFromWatch(msg);
    }

    private boolean isMsgWilling(MSG msg) {

        boolean willing = false;

        synchronized (oCheckTimeOutLock) {
            MSG tmp = mMsgSended.get(msg.stMsgHeader.lId);
            if (tmp != null) {
                mMsgSended.remove(msg.stMsgHeader.lId);
                willing = true;
            }

        }

        return willing;
    }
}
