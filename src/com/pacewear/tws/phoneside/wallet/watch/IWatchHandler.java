
package com.pacewear.tws.phoneside.wallet.watch;

import com.tencent.tws.proto.wallet.WatchNFCManager.MSG;

/**
 * IWatchHandler
 * 
 * @author baodingzhou
 */

public interface IWatchHandler {

    /**
     * registerWatchHandlerListener
     * 
     * @param listener
     */
    public void registerWatchHandlerListener(IWatchHandlerListener listener);

    /**
     * unregisterWatchHandlerListener
     * 
     * @param listener
     */
    public void unregisterWatchHandlerListener(IWatchHandlerListener listener);

    /**
     * sendMsg
     * 
     * @param msg
     * @return
     */
    public boolean sendMsgToWatch(MSG msg);

    /**
     * dispatchMsgFromWatch
     * 
     * @param msg
     * @return
     */
    public boolean dispatchMsgFromWatch(MSG msg);
}
