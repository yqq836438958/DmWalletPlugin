
package com.pacewear.tws.phoneside.wallet.env;

/**
 * @author baodingzhou
 */

public interface IEnvManager {

    /**
     * registerEnvManagerListener
     * 
     * @param listener
     * @return
     */
    public boolean registerEnvManagerListener(IEnvManagerListener listener);

    /**
     * unregisterEnvManagerListener
     * 
     * @param listener
     * @return
     */
    public boolean unregisterEnvManagerListener(IEnvManagerListener listener);

    /**
     * isCPLCReady
     * 
     * @return
     */
    public boolean isCPLCReady();

    /**
     * forceSyncCPLC
     * 
     * @return
     */
    public boolean forceSyncCPLC(boolean isForce);

    /**
     * isWatchConnected
     * 
     * @return
     */
    public boolean isWatchConnected();
}
