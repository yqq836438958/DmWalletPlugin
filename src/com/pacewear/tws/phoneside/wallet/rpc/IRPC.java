
package com.pacewear.tws.phoneside.wallet.rpc;

/**
 * @author baodingzhou
 */

public interface IRPC<Interface> {

    /**
     * putInt
     * 
     * @param i
     * @return
     */
    public IRPC<Interface> putInt(int i);

    /**
     * putString
     * 
     * @param str
     * @return
     */
    public IRPC<Interface> putString(String str);

    /**
     * putBytes
     * 
     * @param bytes
     * @return
     */
    public IRPC<Interface> putBytes(byte[] bytes);

    /**
     * @param strs
     * @return
     */
    public IRPC<Interface> putStringArray(String[] strs);

    /**
     * setTimeoutMillis
     * 
     * @param millis
     * @return
     */
    public IRPC<Interface> setTimeoutMillis(long millis);

    /**
     * method
     * 
     * @param method
     * @return
     */
    public IRPC<Interface> method(int method);

    /**
     * invoke
     * 
     * @param listener
     * @return
     */
    public boolean invoke(Interface listener);

    /**
     * getSeqID
     * 
     * @return
     */
    public long getSeqID();
}
