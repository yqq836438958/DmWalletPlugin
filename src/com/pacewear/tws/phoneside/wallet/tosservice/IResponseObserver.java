
package com.pacewear.tws.phoneside.wallet.tosservice;

import com.qq.taf.jce.JceStruct;

/**
 * @author baodingzhou
 */

public interface IResponseObserver {

    /**
     * onResponseSucceed
     * 
     * @param uniqueSeq
     * @param operType
     * @param response
     */
    public void onResponseSucceed(long uniqueSeq, int operType, JceStruct response);

    /**
     * onResponseFailed
     * 
     * @param uniqueSeq
     * @param operType
     * @param errorCode
     * @param description
     */
    public void onResponseFailed(long uniqueSeq, int operType, int errorCode, String description);

}
