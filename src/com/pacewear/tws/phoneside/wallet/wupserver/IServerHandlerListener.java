
package com.pacewear.tws.phoneside.wallet.wupserver;

/**
 * @author baodingzhou
 */

public interface IServerHandlerListener {

    /**
     * onResponseSucceed
     * 
     * @param reqID
     * @param operType
     * @param response
     * @return
     */
    public boolean onResponseSucceed(int reqID, int operType, byte[] response);

    /**
     * onResponseFailed
     * 
     * @param reqID
     * @param operType
     * @param errorCode
     * @param description
     * @return
     */
    public boolean onResponseFailed(int reqID, int operType, int errorCode, String description);
}
