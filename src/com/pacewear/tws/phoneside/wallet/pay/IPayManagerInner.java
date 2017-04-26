
package com.pacewear.tws.phoneside.wallet.pay;

import java.util.List;

/**
 * @author baodingzhou
 */

public interface IPayManagerInner {
    public static final int PAY_WEIXIN = 0;
    public static final int PAY_QQ = 1;

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

    public List<PayBean> getPayBeans();
}
