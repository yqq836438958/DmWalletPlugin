
package com.pacewear.tws.phoneside.wallet.walletservice;

import com.tencent.tws.proto.wallet.WatchNFCManager.MSG_RPC_METHOD;

import qrom.component.log.QRomLog;

/**
 * @author baodingzhou
 */

public class SelectAid extends WalletService {

    private static final String TAG = "SelectAid";

    private static final long TIMEOUT_MILLIS = 5000;

    @Override
    public boolean invoke(IResult listener) {
        QRomLog.d(TAG, "invoke");
        method(MSG_RPC_METHOD._SELECT_AID);
        setTimeoutMillis(TIMEOUT_MILLIS);
        return super.invoke(listener);
    }

}
