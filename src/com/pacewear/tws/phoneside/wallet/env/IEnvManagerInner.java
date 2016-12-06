
package com.pacewear.tws.phoneside.wallet.env;

import TRom.DeviceBaseInfo;
import TRom.PayReqHead;
import TRom.SEBaseInfo;
import TRom.WalletBaseInfo;
import TRom.WatchPayUserAuthInfo;

/**
 * @author baodingzhou
 */

public interface IEnvManagerInner extends IEnvManager {
    /**
     * getUserAuthInfo
     * 
     * @return
     */
    public WatchPayUserAuthInfo getUserAuthInfo();

    /**
     * getDeviceBaseInfo
     * 
     * @return
     */
    public DeviceBaseInfo getDeviceBaseInfo();

    /**
     * getSeBaseInfo
     * 
     * @return SEBaseInfo or null if cplc not ready
     */
    public SEBaseInfo getSeBaseInfo();

    /**
     * getPayReqHead
     * 
     * @return PayReqHead or null if cplc not ready
     */
    public PayReqHead getPayReqHead();

    /**
     * getWalletBaseInfo
     * 
     * @return
     */
    public WalletBaseInfo getWalletBaseInfo();

    /**
     * getUserPhoneNum
     * 
     * @return
     */
    public String getUserPhoneNum();

    /**
     * setUserPhoneNum
     * 
     * @return
     */
    public void setUserPhoneNum(String phone);
}
