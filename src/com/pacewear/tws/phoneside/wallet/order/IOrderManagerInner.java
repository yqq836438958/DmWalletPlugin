
package com.pacewear.tws.phoneside.wallet.order;

import com.pacewear.tws.phoneside.wallet.order.IOrderManagerListener.MODULE;
import com.pacewear.tws.phoneside.wallet.step.IStep.COMMON_STEP;
import com.pacewear.tws.phoneside.wallet.step.IStep.STATUS;

import TRom.PayConfig;

/**
 * @author baodingzhou
 */

public interface IOrderManagerInner extends IOrderManager {

    /**
     * getOrder
     * 
     * @param tradeNo
     * @return
     */
    public IOrderInner getOrderInner(String tradeNo);

    /**
     * getPayConfig
     * 
     * @param aid
     * @return
     */
    public PayConfig getPayConfig(String aid);

    /**
     * addOrder
     * 
     * @param order
     */
    public void addOrder(IOrderInner order);

    /**
     * clearOrder
     * 
     * @param order
     */
    public void clearOrder(IOrderInner order);

    /**
     * notifyNewOrder
     * 
     * @param uniqueSeq
     * @param succeed
     * @param tradeNo
     */
    public void notifyNewOrder(long uniqueSeq, boolean succeed, String tradeNo);

    /**
     * notifyOrderStatus
     * 
     * @param tradeNo
     * @param step
     * @param status
     */
    public void notifyOrderStatus(String tradeNo, ORDER_STEP step, STATUS status);

    /**
     * notifyOrderModuleStatus
     * 
     * @param module
     * @param step
     * @param status
     */
    public void notifyOrderModuleStatus(MODULE module, COMMON_STEP step, STATUS status);
}
