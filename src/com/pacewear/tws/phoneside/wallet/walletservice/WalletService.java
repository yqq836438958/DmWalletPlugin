
package com.pacewear.tws.phoneside.wallet.walletservice;

import com.pacewear.tws.phoneside.wallet.rpc.RPCMethod;
import com.tencent.tws.proto.wallet.WatchNFCManager.MSG;
import com.tencent.tws.proto.wallet.WatchNFCManager.MSG_RPC_PARAMS;

import qrom.component.log.QRomLog;

/**
 * @author baodingzhou
 */

public abstract class WalletService extends RPCMethod<IResult> {

    private static final String TAG = "WalletService";

    @Override
    public final void onResultReal(MSG_RPC_PARAMS result) {
        QRomLog.d(TAG, "onResultReal");
        String[] outputParams = result.getVString() != null ? result.getVString().toArray(
                new String[0]) : null;
        Integer[] resultCode = result.getVInt() != null ? result.getVInt().toArray(new Integer[0])
                : null;
        mListener.onResult(getSeqID(), result.getIInt(), outputParams, resultCode,
                result.getVBytes());
    }

    @Override
    public final void onExecptionReal(MSG msg) {
        QRomLog.d(TAG, "onExecptionReal");
        mListener.onExecption(getSeqID(), msg.stMsgHeader.eState);
    }
}
