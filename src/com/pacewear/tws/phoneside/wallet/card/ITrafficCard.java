
package com.pacewear.tws.phoneside.wallet.card;

import TRom.BusCardBaseInfo;

/**
 * @author baodingzhou
 */

public interface ITrafficCard extends ICard {

    /**
     * getBusCardBaseInfo
     * 
     * @return
     */
    public BusCardBaseInfo getBusCardBaseInfo();

    /**
     * getBalance
     * 
     * @return 余额(单位分)
     */
    public String getBalance();

    /**
     * getValidity
     *
     * @return 有效期
     */
    public String getValidity();

    /**
     * getStartDate
     *
     * @return 启用日期
     */
    public String getStartDate();
}
