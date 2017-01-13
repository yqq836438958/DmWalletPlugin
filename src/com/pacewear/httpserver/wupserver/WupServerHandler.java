
package com.pacewear.httpserver.wupserver;

import android.content.Context;
import android.util.Log;

import com.pacewear.httpserver.IServerHandlerListener;
import com.pacewear.httpserver.ServerHandler;
import com.qq.jce.wup.UniPacket;

import java.util.ArrayList;

import qrom.component.wup.QRomComponentWupManager;
import qrom.component.wup.QRomWupReqExtraData;
import qrom.component.wup.QRomWupRspExtraData;

/**
 * @author baodingzhou
 */

public class WupServerHandler extends ServerHandler {

    private static final String TAG = "ServerHandler";

    private static final int WUP_MOUDLE_ID = 11;

    private QRomComponentWupManager mQRomComponentWupManager = null;

    public WupServerHandler(Context context) {
        super(context);
        Log.d(TAG, "WupServerHandler");
        mQRomComponentWupManager = new QRomComponentWupManager() {

            @Override
            public void onReceiveError(int fromModelType, int reqId, int operType,
                    QRomWupReqExtraData wupReqExtraData,
                    QRomWupRspExtraData wupRspExtraData, String serviceName, int errorCode,
                    String description) {
                Log.d(TAG, String.format(
                        "onReceiveError reqId:%d operType:%d serviceName:%s errorCode:%d description:%s",
                        reqId, operType, serviceName, errorCode, description));

                onResponseFailed(reqId, operType, errorCode, description);
            }

            @Override
            public void onReceiveAllData(int fromModelType, int reqId, int operType,
                    QRomWupReqExtraData wupReqExtraData,
                    QRomWupRspExtraData wupRspExtraData, String serviceName, byte[] response) {
                Log.d(TAG, String.format(
                        "onReceiveAllData reqId:%d operType:%d serviceName:%s byte count:%d", reqId,
                        operType, serviceName, response.length));
                onResponseSucceed(reqId, operType, response);
            }

            @Override
            public void onGuidChanged(byte[] arg0) {
                Log.d(TAG, "ServerHandler");
            }
        };
        mQRomComponentWupManager.startup(context);
    }

    @Override
    public int reqServer(int operType, UniPacket uniPacket) {
        Log.d(TAG, "reqServer operType:" + operType);
        return mQRomComponentWupManager.requestWupNoRetry(WUP_MOUDLE_ID, operType, uniPacket, null,
                0, mRequestEncrypt);
    }

    @Override
    public boolean isTestEnv() {
        return mQRomComponentWupManager.getWupEtcWupEnviFlg() == 1;
    }

    @Override
    public void setRequestEncrypt(boolean encrypt) {
        mRequestEncrypt = encrypt;
    }
}
