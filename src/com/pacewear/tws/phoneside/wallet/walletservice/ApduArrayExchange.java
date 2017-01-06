
package com.pacewear.tws.phoneside.wallet.walletservice;

import com.tencent.tws.proto.wallet.WatchNFCManager.MSG_RPC_METHOD;

import qrom.component.log.QRomLog;

public class ApduArrayExchange extends WalletService {
    private static final String TAG = "ApduArrayExchange";

    private static final long TIMEOUT_MILLIS = 60000;

    @Override
    public boolean invoke(IResult listener) {
        QRomLog.d(TAG, "invoke");
        method(MSG_RPC_METHOD._APDUARRAY_EXCHANGE);
        setTimeoutMillis(TIMEOUT_MILLIS);
        return super.invoke(listener);
    }

}
