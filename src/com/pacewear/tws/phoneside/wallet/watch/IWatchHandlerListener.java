
package com.pacewear.tws.phoneside.wallet.watch;

import com.tencent.tws.proto.wallet.WatchNFCManager.MSG;

/**
 * @author baodingzhou
 */

public interface IWatchHandlerListener {

    /**
     * onMsgReceived
     * 
     * @param msg
     * @return
     */
    public boolean onMsgReceived(MSG msg);

}
