
package com.pacewear.tws.phoneside.wallet.walletservice;

import com.tencent.tws.proto.wallet.WatchNFCManager.MSG_RPC_METHOD;

import qrom.component.log.QRomLog;

/**
 * @author baodingzhou
 */

public class CardQuery extends WalletService {

    private static final String TAG = "CardQuery";

    private static final long TIMEOUT_MILLIS = 24000;

    @Override
    public boolean invoke(IResult listener) {
        QRomLog.d(TAG, "invoke");
        method(MSG_RPC_METHOD._CARD_QUERY);
        setTimeoutMillis(TIMEOUT_MILLIS);
        return super.invoke(listener);
    }
}
