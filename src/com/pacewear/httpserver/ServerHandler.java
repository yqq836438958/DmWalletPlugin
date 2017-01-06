
package com.pacewear.httpserver;

import com.pacewear.tws.phoneside.wallet.WalletApp;
import com.qq.jce.wup.UniPacket;

import java.util.ArrayList;
import java.util.Iterator;

import qrom.component.log.QRomLog;
import qrom.component.wup.QRomComponentWupManager;
import qrom.component.wup.QRomWupReqExtraData;
import qrom.component.wup.QRomWupRspExtraData;

/**
 * @author baodingzhou
 */

public class ServerHandler extends QRomComponentWupManager implements IServerHandler,
        IServerHandlerListener {

    private static final String TAG = "ServerHandler";

    private static final int WUP_MOUDLE_ID = 11;

    public static volatile IServerHandler sInstance = null;

    private ArrayList<IServerHandlerListener> mListener = new ArrayList<IServerHandlerListener>();

    private boolean mRequestEncrypt = false;

    public ServerHandler() {
        QRomLog.d(TAG, "ServerHandler");
        startup(WalletApp.sGlobalCtx);
    }

    public static IServerHandler getInstance() {
        if (sInstance == null) {
            synchronized (ServerHandler.class) {
                if (sInstance == null) {
                    sInstance = new ServerHandler();
                }
            }
        }

        return sInstance;
    }

    @Override
    public void onGuidChanged(byte[] arg0) {
        QRomLog.d(TAG, "ServerHandler");
    }

    @Override
    public void onReceiveAllData(int fromModelType, int reqId, int operType,
            QRomWupReqExtraData wupReqExtraData,
            QRomWupRspExtraData wupRspExtraData, String serviceName, byte[] response) {
        QRomLog.d(TAG, String.format(
                "onReceiveAllData reqId:%d operType:%d serviceName:%s byte count:%d", reqId,
                operType, serviceName, response.length));

        onResponseSucceed(reqId, operType, response);
    }

    @Override
    public void onReceiveError(int fromModelType, int reqId, int operType,
            QRomWupReqExtraData wupReqExtraData,
            QRomWupRspExtraData wupRspExtraData, String serviceName, int errorCode,
            String description) {
        QRomLog.d(TAG, String.format(
                "onReceiveError reqId:%d operType:%d serviceName:%s errorCode:%d description:%s",
                reqId, operType, serviceName, errorCode, description));

        onResponseFailed(reqId, operType, errorCode, description);
    }

    @Override
    public int reqServer(int operType, UniPacket uniPacket) {
        QRomLog.d(TAG, "reqServer operType:" + operType);
        return requestWupNoRetry(WUP_MOUDLE_ID, operType, uniPacket, null, 0, mRequestEncrypt);
    }

    @Override
    public boolean registerServerHandlerListener(IServerHandlerListener listener) {
        QRomLog.d(TAG, "registerServerHandlerListener");
        synchronized (mListener) {
            if (!mListener.contains(listener)) {
                return mListener.add(listener);
            }

        }
        return false;
    }

    @Override
    public boolean unregisterServerHandlerListener(IServerHandlerListener listener) {
        QRomLog.d(TAG, "unregisterServerHandlerListener");
        synchronized (mListener) {
            return mListener.remove(listener);
        }
    }

    @Override
    public boolean onResponseSucceed(int reqID, int operType, byte[] response) {
        QRomLog.d(TAG, "onResponseSucceed");
        boolean handle = false;
        Iterator<IServerHandlerListener> iterator = null;
        IServerHandlerListener listener = null;
        synchronized (mListener) {
            iterator = mListener.iterator();
            if (iterator != null) {
                while (iterator.hasNext()) {
                    listener = iterator.next();
                    handle = listener.onResponseSucceed(reqID, operType, response);
                    if (handle) {
                        break;
                    }
                }
            }
        }

        if (handle) {
            unregisterServerHandlerListener(listener);
        }

        return handle;
    }

    @Override
    public boolean onResponseFailed(int reqID, int operType, int errorCode, String description) {
        QRomLog.d(TAG, "onResponseFailed");
        boolean handle = false;
        Iterator<IServerHandlerListener> iterator = null;
        IServerHandlerListener listener = null;
        synchronized (mListener) {
            iterator = mListener.iterator();
            if (iterator != null) {
                while (iterator.hasNext()) {
                    listener = iterator.next();
                    handle = listener.onResponseFailed(reqID, operType, errorCode, description);
                    if (handle) {
                        break;
                    }
                }
            }
        }

        if (handle) {
            unregisterServerHandlerListener(listener);
        }

        return handle;
    }

    @Override
    public boolean isTestEnv() {
        return getWupEtcWupEnviFlg() == 1;
    }

    @Override
    public void setRequestEncrypt(boolean encrypt) {
        mRequestEncrypt = encrypt;
    }
}
