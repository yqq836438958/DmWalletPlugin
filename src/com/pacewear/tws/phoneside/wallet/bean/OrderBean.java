
package com.pacewear.tws.phoneside.wallet.bean;

import java.io.Serializable;

public class OrderBean implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private String sAid;
    private int iCardType;
    private int iPaySene;
    private int iPayType;
    private long lIssueFee;
    private long lTopupFee;
    private long lTotalFee;
    private boolean bRetry;

    public String getCardInstanceId() {
        return sAid;
    }

    public int getCardType() {
        return iCardType;
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

    public long getTotalFee() {
        return lTopupFee;
    }

    public boolean isRetry() {
        return bRetry;
    }
}
