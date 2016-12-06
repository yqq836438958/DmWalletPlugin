
package com.pacewear.tws.phoneside.wallet.order;

import com.pacewear.tws.phoneside.wallet.order.IOrderManager.ORDER_STEP;

import TRom.GetPayResultRspParam;
import TRom.OrderReqParam;
import TRom.OrderRspParam;

/**
 * @author baodingzhou
 */

public interface IOrder {

    /**
     * isLocal 本地订单
     * 
     * @return
     */
    public boolean isLocal();

    /**
     * getOrderRspParam
     * 
     * @return
     */
    public OrderRspParam getOrderRspParam();

    /**
     * getOrderReqParam
     * 
     * @return
     */
    public OrderReqParam getOrderReqParam();

    /**
     * getGetPayResultRspParam
     * 
     * @return
     */
    public GetPayResultRspParam getGetPayResultRspParam();

    /**
     * isIssueFail
     * 
     * @return
     */
    public boolean isIssueFail();

    /**
     * isCardTopFail
     * 
     * @return
     */
    public boolean isCardTopFail();

    public boolean isInValidOrder();

    /**
     * getRetryTime
     * 
     * @return
     */
    public int getRetryTime();

    public ORDER_STEP getOrderStep();

    boolean isIdle();

    public String getBusinessErr();
}
