
package com.pacewear.tsm.internal.core;

import com.pacewear.httpserver.IResponseObserver;
import com.pacewear.tsm.channel.ITsmAPDUCallback;
import com.pacewear.tsm.channel.ITsmCardChannel;
import com.pacewear.tsm.common.UniSessionStore;
import com.pacewear.tsm.server.tosservice.TSMTosService;
import com.qq.taf.jce.JceStruct;

import java.util.ArrayList;
import java.util.List;

public class ApduTransmiter implements IApduTransmiter {
    private IProcessEventConsum mConsumer = null;
    private ITsmCardChannel mCardChannel = null;

    public ApduTransmiter(ITsmCardChannel cardChannel, IProcessEventConsum event) {
        mCardChannel = cardChannel;
        mConsumer = event;
    }

    @Override
    public boolean transmit(TSMTosService service, final OnTsmProcessCallback callback) {
        List<String> localList = new ArrayList<String>();
        mConsumer.onParserApdu(null, localList, true);
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
                    int iRet = mConsumer.onParserApdu(response, apdulist, false);
                    if (iRet != 0) {
                        callback.onFail(-1, "");
                        return;
                    }
                    // 若任务无需再发apdu指令，那么在此进行结束
                    if (mConsumer.returnWithoutTransmit()) {
                        callback.onSuccess(null);
                        return;
                    }
                    if (apdulist == null || apdulist.size() <= 0) {
                        callback.onFail(-1, "");
                        return;
                    }
                    onTransmit(apdulist, callback);
                }
            }

            @Override
            public void onResponseFailed(long uniqueSeq, int operType, int errorCode,
                    String description) {
                if (uniqReq == uniqueSeq) {
                    callback.onFail(errorCode, description);
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
                callback.onSuccess(apdus);
            }

            @Override
            public void onFail(int error, String desc) {
                callback.onFail(error, desc);
            }
        });
    }

    @Override
    public boolean close() {
        UniSessionStore.getInstance().clear();
        return mCardChannel.close();
    }

    @Override
    public boolean selectAID(String aid, final OnTsmProcessCallback callback) {
        if (mCardChannel == null) {
            return false;
        }
        return mCardChannel.selectAID(aid, new ITsmAPDUCallback() {

            @Override
            public void onSuccess(String[] apdus) {
                if (callback != null) {
                    callback.onSuccess(apdus);
                }
                UniSessionStore.getInstance().clear();
            }

            @Override
            public void onFail(int error, String desc) {
                if (callback != null) {
                    callback.onFail(error, desc);
                }
            }
        });
    }
}
