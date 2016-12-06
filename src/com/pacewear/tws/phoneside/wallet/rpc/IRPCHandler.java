
package com.pacewear.tws.phoneside.wallet.rpc;

import com.tencent.tws.proto.wallet.WatchNFCManager.MSG_RPC;

/**
 * @author baodingzhou
 */

public interface IRPCHandler {

    /**
     * registerIRPCHandlerListener
     * 
     * @param listener
     * @return
     */
    public boolean registerIRPCHandlerListener(IRPCHandlerListener listener);

    /**
     * unregisterIRPCHandlerListener
     * 
     * @param listener
     * @return
     */
    public boolean unregisterIRPCHandlerListener(IRPCHandlerListener listener);

    /**
     * sendRPCMsg
     * 
     * @param seqID
     * @param rpc
     * @param timeoutMills
     * @return
     */
    public boolean sendRPCMsg(long seqID, MSG_RPC rpc, long timeoutMills);
}
