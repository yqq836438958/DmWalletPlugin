
package com.pacewear.tws.phoneside.wallet.env;

/**
 * @author baodingzhou
 */

public interface IEnvManagerListener {

    /**
     * onWatchConnection
     * 
     * @param connected
     * @return
     */
    public boolean onWatchConnection(boolean connected);

    /**
     * onWatchIdentified
     * 
     * @param succeed
     * @param cplc
     * @return
     */
    public boolean onWatchIdentified(boolean succeed, String cplc);

    /**
     * onNewWatchPaired
     * 
     * @return
     */
    public boolean onNewWatchPaired();

    /**
     * onOldWatchUnpaired
     * 
     * @return
     */
    public boolean onOldWatchUnpaired();
}
