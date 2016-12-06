
package com.pacewear.tws.phoneside.wallet.rpc;

import com.tencent.tws.proto.wallet.WatchNFCManager.MSG;
import com.tencent.tws.proto.wallet.WatchNFCManager.MSG_RPC_PARAMS;

/**
 * @author baodingzhou
 */

public interface IRPCHandlerListener {

    /**
     * onResult
     * 
     * @param result
     */
    public boolean onResult(long seq, MSG_RPC_PARAMS result);

    /**
     * onExecption
     * 
     * @param msg
     * @return
     */
    public boolean onExecption(long seq, MSG msg);
}
