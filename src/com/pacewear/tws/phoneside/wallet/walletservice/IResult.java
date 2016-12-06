
package com.pacewear.tws.phoneside.wallet.walletservice;

/**
 * @author baodingzhou
 */

public interface IResult {

    /**
     * onResult
     * 
     * @param seqID
     * @param ret
     * @param outputParams
     * @param resultCode
     * @param bytes
     */
    public void onResult(long seqID, int ret, String[] outputParams, Integer[] resultCode,
            byte[] bytes);

    /**
     * onExecption
     * 
     * @param error
     */
    public void onExecption(long seqID, int error);
}
