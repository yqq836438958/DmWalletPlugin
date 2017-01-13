
package com.pacewear.httpserver;

import android.content.Context;
import android.util.Base64;
import android.util.Log;

import com.pacewear.httpserver.wupserver.WupServerHandler;
import com.pacewear.tsm.common.RunEnv;
import com.tencent.tws.api.HttpRequestCommand;

import java.util.ArrayList;
import java.util.Iterator;

import qrom.component.log.QRomLog;
import qrom.component.wup.base.utils.ZipUtils;

/**
 * @author baodingzhou
 */

abstract public class ServerHandler implements IServerHandler,
        IServerHandlerListener {

    private static final String TAG = "ServerHandler";

    private static final int WUP_MOUDLE_ID = 11;

    private static volatile IServerHandler sInstance = null;

    private ArrayList<IServerHandlerListener> mListener = new ArrayList<IServerHandlerListener>();

    protected boolean mRequestEncrypt = false;

    private IHttpPost mHttpPost = null;

    public ServerHandler(Context context, IHttpPost _httpPost) {
        this(context);
        mHttpPost = _httpPost;
    }

    public ServerHandler(Context context) {
        Log.d(TAG, "ServerHandler");
    }

    public static IServerHandler getInstance(Context context) {
        if (sInstance == null) {
            synchronized (ServerHandler.class) {
                if (sInstance == null) {
                    switch (RunEnv.getPlatform()) {
                        case RunEnv.PLATFORM_DM:
                            sInstance = new WupServerHandler(context);
                            break;
                        case RunEnv.PLATFORM_WATCH:
                            // sInstance = new DmaServerHandler(context);
                            break;
                        default:
                            break;
                    }
                }
            }
        }

        return sInstance;
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
    public void setRequestEncrypt(boolean encrypt) {
        mRequestEncrypt = encrypt;
    }

    protected void wrapHttpPost(byte[] dataPacket) {
        mHttpPost.addHeader(HttpHeader.REQ.CONTENT_TYPE, HttpHeader.CONTENT_TYPE);
        mHttpPost.addHeader(HttpHeader.REQ.HOST, HttpUrl.get().getHttpHost());
        mHttpPost.addHeader(HttpHeader.REQ.QGUID, "12C7CF7026FDB81AF0F93F1FB281F5C7"); // TODO
        mHttpPost.addHeader(HttpHeader.REQ.ACCEPT_ENCODING, HttpHeader.WUP_HEADER_GZIP_VALUE);
        mHttpPost.addHeader(HttpHeader.REQ.QQ_S_ZIP, HttpHeader.WUP_HEADER_GZIP_VALUE);
        mHttpPost.addBody(ZipUtils.gZip(dataPacket));
    }

    protected byte[] onParseHttpRsp(String rsp) {
        byte[] decodeRsp = Base64.decode(rsp, Base64.DEFAULT);
        if (decodeRsp == null) {
            return null;
        }
        byte[] result = ZipUtils.unGzip(decodeRsp);
        return result;
    }

    protected Object getHttpPostContent() {
        return mHttpPost.get();
    }
}
