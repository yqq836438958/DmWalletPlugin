
package com.pacewear.tws.phoneside.wallet.order;

import com.pacewear.tws.phoneside.wallet.order.IOrderManager.ORDER_STEP;
import com.pacewear.tws.phoneside.wallet.step.IStep.COMMON_STEP;
import com.pacewear.tws.phoneside.wallet.step.IStep.STATUS;

/**
 * @author baodingzhou
 */

public interface IOrderManagerListener {

    /**
     * onNewOrder
     * 
     * @param uniqueSeq
     * @param succeed
     * @param tradeNo
     */
    public void onNewOrder(long uniqueSeq, boolean succeed, String tradeNo);

    /**
     * onOrderStatus
     * 
     * @param tradeNo
     * @param step
     * @param status
     */
    public void onOrderStatus(String tradeNo, ORDER_STEP step, STATUS status);

    /**
     * MODULE
     */
    public enum MODULE {
        ORDER_LIST, TRAFFIC_CONFIG,
    }

    /**
     * onOrderManager
     * 
     * @param module
     * @param step
     * @param status
     */
    public void onOrderManager(MODULE module, COMMON_STEP step, STATUS status);
}
