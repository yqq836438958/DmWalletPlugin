
package com.pacewear.tws.phoneside.wallet.order;

/**
 * @author baodingzhou
 */

public interface IOrderManager {

    /**
     * 订单阶段
     */
    public enum ORDER_STEP {
        /**
         * 下单
         */
        PLACE_ORDER,
        /**
         * 订单支付
         */
        PAY_ORDER,

        /**
         * 订单支付确认
         */
        ORDER_PAID_CONFIRM,

        /**
         * 订单支付失败
         */
        ORDER_PAID_FAILED,

        /**
         * 订单支付成功
         */
        ORDER_PAID_SUCCEED,

        /**
         * 获取业务订单
         */
        OBTAIN_BUSSINESS_ORDER,

        /**
         * 执行开卡
         */
        EXECUTE_ISSUE,

        /**
         * 执行充值
         */
        EXECUTE_TOPUP,

        /**
         * 订单最后状态确认
         */
        ORDER_FINAL_CONFIRM,

        /**
         * 订单结束
         */
        ORDER_FINISH,
    }

    /**
     * registerOrderManagerListener
     * 
     * @param listener
     * @return
     */
    public boolean registerOrderManagerListener(IOrderManagerListener listener);

    /**
     * unregisterOrderManagerListener
     * 
     * @param listener
     * @return
     */
    public boolean unregisterOrderManagerListener(IOrderManagerListener listener);

    /**
     * getOrders
     * 
     * @param aid
     * @return
     */
    public IOrder[] getOrders(String aid);

    /**
     * getOrders
     * 
     * @param aid
     * @return
     */
    public IOrder getLastOrder(String aid);

    /**
     * getOrder
     * 
     * @param tradeNo
     * @return
     */
    public IOrder getOrder(String tradeNo);

    /**
     * setOrderLocalPaidStatus
     * 
     * @param tradeNo
     * @param paid
     */
    public void setOrderLocalPaidStatus(String tradeNo, boolean paid);

    /**
     * placeIssueOrder 新建开卡订单
     * 
     * @param aid
     * @param payType 支付类型
     * @param activteFee 开卡费
     * @param chargeValue 充值金额
     * @return
     */
    public long placeIssueOrder(String aid, int payScene, int payType, long activateFee, long chargeValue,boolean retry);

    /**
     * placeTopupOrder
     * 
     * @param aid
     * @param payType
     * @return
     */
    public long placeTopupOrder(String aid, int payScene, int payType, long chargeValue,boolean retry);

    /**
     * isTrafficConfigReady
     * 
     * @return
     */
    public boolean isTrafficConfigReady();

    /**
     * forceSyncTrafficConfig
     * 
     * @return
     */
    public boolean forceSyncTrafficConfig(boolean isForce);

    /**
     * isOrderReady
     * 
     * @return
     */
    public boolean isOrderReady();

    /**
     * forceSyncOrder
     * 
     * @return
     */
    public boolean forceSyncOrder(boolean isForce);

    /**
     * isInSyncProcess
     *
     * @return
     */
    public boolean isInOrderSyncProcess();
}
