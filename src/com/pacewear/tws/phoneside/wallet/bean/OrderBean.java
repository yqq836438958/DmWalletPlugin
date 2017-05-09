
package com.pacewear.tws.phoneside.wallet.bean;

import android.R.integer;

import com.pacewear.tws.phoneside.wallet.order.IOrder;

import java.io.Serializable;

import TRom.OrderReqParam;

public class OrderBean implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private String sAid;
    private int iPaySene;
    private int iPayType;
    private long lIssueFee;
    private long lTopupFee;
    private boolean bRetry;

    public String getCardInstanceId() {
        return sAid;
    }

    public int getPaySene() {
        return iPaySene;
    }

    public int getPayType() {
        return iPayType;
    }

    public long getIssueFee() {
        return lIssueFee;
    }

    public long getTopupFee() {
        return lTopupFee;
    }

    public boolean isRetry() {
        return bRetry;
    }

    public void setRetry(boolean retry) {
        bRetry = retry;
    }

    public static OrderBean genNewInstance(String aid, int payType, int scene,
            long actFee, long chargeFee,
            boolean isRetry) {
        OrderBean bean = new OrderBean();
        bean.sAid = aid;
        bean.iPayType = payType;
        bean.iPaySene = scene;
        bean.lIssueFee = actFee;
        bean.lTopupFee = chargeFee;
        return bean;
    }

    public static OrderBean genByLastOrder(String aid, IOrder order) {
        OrderBean bean = new OrderBean();
        OrderReqParam reqParam = order.getOrderReqParam();
        bean.sAid = aid;
        bean.iPayType = reqParam.getEPayType();
        bean.iPaySene = reqParam.getEPayScene();
        bean.lIssueFee = reqParam.getIOpenCardFee();
        bean.lTopupFee = reqParam.getITotalFee();
        bean.bRetry = true;
        return bean;
    }
}
