
package com.pacewear.tws.phoneside.wallet.pay;

/**
 * @author baodingzhou
 */

public interface IPayManagerInner {

    /**
     * pay
     * 
     * @param tradeNo
     * @param listener
     * @return
     */
    public boolean pay(String tradeNo);

    /**
     * isPaying
     * 
     * @return
     */
    public boolean isPaying();

    /**
     * cancelPay
     * 
     * @return
     */
    public void cancelPay();
}
