
package com.pacewear.tws.phoneside.wallet.sdkadapter;

import com.pacewear.tsm.channel.ITsmAPDUCallback;
import com.pacewear.tsm.channel.ITsmCardChannel;
import com.pacewear.tsm.common.ByteUtil;
import com.pacewear.tws.phoneside.wallet.walletservice.ApduArrayExchange;
import com.pacewear.tws.phoneside.wallet.walletservice.GetCPLC;
import com.pacewear.tws.phoneside.wallet.walletservice.IResult;
import com.pacewear.tws.phoneside.wallet.walletservice.SelectAid;
import com.pacewear.tws.phoneside.wallet.walletservice.ShutDown;
import com.pacewear.tws.phoneside.wallet.walletservice.WalletService;

import java.util.List;

public class SnowBallCardChannel implements ITsmCardChannel {
    private static SnowBallCardChannel sInstance = null;

    public static SnowBallCardChannel get() {
        if (sInstance == null) {
            synchronized (SnowBallCardChannel.class) {
                sInstance = new SnowBallCardChannel();
            }
        }
        return sInstance;
    }

    private SnowBallCardChannel() {

    }
    @Override
    public boolean getCPLC(ITsmAPDUCallback callback) {
        GetCPLC getCPLC = new GetCPLC();
        return transmit_Internal(getCPLC, callback);
    }

    @Override
    public boolean transmit(List<String> apduList, ITsmAPDUCallback callback) {
        ApduArrayExchange exchange = new ApduArrayExchange();
        String[] params = new String[apduList.size()];
        apduList.toArray(params);
        exchange.putStringArray(params);
        return transmit_Internal(exchange, callback);
    }

    @Override
    public boolean close() {
        ShutDown shutDown = new ShutDown();
        shutDown.invoke(new IResult() {

            @Override
            public void onResult(long seqID, int ret, String[] outputParams, Integer[] resultCode,
                    byte[] bytes) {

            }

            @Override
            public void onExecption(long seqID, int error) {
            }
        });
        return true;
    }

    @Override
    public boolean selectAID(String aid, ITsmAPDUCallback callback) {
        SelectAid selectAid = new SelectAid();
        selectAid.putString(aid);
        return transmit_Internal(selectAid, callback);
    }

    private final boolean transmit_Internal(final WalletService service,
            final ITsmAPDUCallback callback) {
        final long uniqReq = service.getSeqID();
        return service.invoke(new IResult() {

            @Override
            public void onResult(long seqID, int ret, String[] outputParams, Integer[] resultCode,
                    byte[] bytes) {
                if (uniqReq == seqID) {
                    if (ret == 0) {
                        if (outputParams != null && outputParams.length > 0) {
                            if ((service instanceof ApduArrayExchange)
                                    && (!ByteUtil.isResponseSuccess(outputParams[0]))) {
                                onPostFail(callback, ret, outputParams[0]);
                                return;
                            }
                        }
                        onPostSuccess(callback, outputParams);
                    } else {
                        if (outputParams != null && outputParams.length > 0) {
                            if ((service instanceof ApduArrayExchange)
                                    && (ByteUtil.isResponseSuccess(outputParams[0]))) {
                                onPostSuccess(callback, outputParams);
                                return;
                            }
                        }
                        onPostFail(callback, ret, outputParams[0]);
                    }
                }
            }

            @Override
            public void onExecption(long seqID, int error) {
                if (uniqReq == seqID) {
                    onPostFail(callback, error, null);
                }
            }
        });
    }

    private void onPostSuccess(ITsmAPDUCallback callback, String[] outputParams) {
        if (callback != null) {
            callback.onSuccess(outputParams);
        }
    }

    private void onPostFail(ITsmAPDUCallback callback, int err, String desc) {
        if (callback != null) {
            callback.onFail(err, desc);
        }
    }

}
