
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
     * getBalanceTextColor
     * 
     * @return 余额颜色
     */
    public int getBalanceTextColor();

    /**
     * getBalanceUnitIcon
     * 
     * @return 余额图标
     */
    public int getBalanceUnitIcon();

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
