
package com.pacewear.tws.phoneside.wallet.order;

/**
 * @author baodingzhou
 */

public interface IOrderInner extends IOrder {

    /**
     * setLocalPaid
     * 
     * @param paid true:已支付 false:未支付
     */
    public void setLocalPaid(boolean paid);

    public int onRetry();
}
