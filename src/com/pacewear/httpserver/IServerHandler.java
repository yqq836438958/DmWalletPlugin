
package com.pacewear.httpserver;

import com.qq.jce.wup.UniPacket;

/**
 * @author baodingzhou
 */

public interface IServerHandler {

    /**
     * reqServer
     * 
     * @param operType
     * @param uniPacket
     * @return
     */
    public int reqServer(int operType, UniPacket uniPacket);

    /**
     * registerServerHandlerListener
     * 
     * @param listener
     * @return
     */
    public boolean registerServerHandlerListener(IServerHandlerListener listener);

    /**
     * unregisterServerHandlerListener
     * 
     * @param listener
     * @return
     */
    public boolean unregisterServerHandlerListener(IServerHandlerListener listener);

    /**
     * isTestEnv
     *
     * @return
     */
    public boolean isTestEnv();

    /**
     * setEncrypt
     */
    public void setRequestEncrypt(boolean encrypt);
}
