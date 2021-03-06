
package com.pacewear.tws.phoneside.wallet.card;

/**
 * @author baodingzhou
 */

public interface ICard {

    /**
     * 卡类型
     */
    public static enum CARD_TYPE {
        TRAFFIC_CARD(0), BANK_CARD(1);
        private int value;

        CARD_TYPE(int num) {
            this.value = num;
        }

        public int toValue() {
            return value;
        }
    }

    /**
     * 卡片安装状态
     */
    public enum INSTALL_STATUS {

        /**
         * 未安装
         */
        UNINSTALLED,

        /**
         * 已安装
         */
        INSTALLED,

        /**
         * 已个人化
         */
        PERSONAL,
    }

    /**
     * 卡片激活状态
     */
    public enum ACTIVATION_STATUS {

        /**
         * 未激活
         */
        UNACTIVATED,

        /**
         * 已激活
         */
        ACTIVATED,
    }

    /**
     * getCardType
     * 
     * @return
     */
    public CARD_TYPE getCardType();

    /**
     * getAID
     * 
     * @return
     */
    public String getAID();

    /**
     * getInstallStatus
     * 
     * @return
     */
    public INSTALL_STATUS getInstallStatus();

    /**
     * getActivationStatus
     * 
     * @return
     */
    public ACTIVATION_STATUS getActivationStatus();

    /**
     * getCardName
     * 
     * @return
     */
    public String getCardName();

    /**
     * isReady
     * 
     * @return
     */
    public boolean isReady();

    /**
     * isAvaliable
     * 
     * @return
     */
    public boolean isAvaliable();

    public boolean isInSyncing();
    /**
     * forceUpdate
     * 
     * @return
     */
    public void forceUpdate();

    /**
     * setDefaultCard
     * 
     * @return
     */
    public boolean setDefaultCard();

    /**
     * getCardICon
     * 
     * @return
     */
    public int getCardICon();

    /**
     * getCardBg
     * 
     * @return
     */
    public int getCardBg();

    /**
     * getCardDisableBg
     * 
     * @return
     */
    public int getCardDisableBg();

    public int getCardLiteBg();

    public int getCardDisableLiteBg();

    /**
     * getExtra_Info
     * 
     * @return
     */
    public String getExtra_Info();

    /**
     * getExtra_Info
     * 
     * @return
     */
    public void setExtra_Info(String key, String value);

    /**
     * getCardNumber
     * 
     * @return 卡号
     */
    public String getCardNumber();

    /**
     * clear
     */
    public void clear();

    /**
     * setCardInfoErrCode 设置卡号信息错误码
     */
    public void setCardInfoErrCode(int code);

    /**
     * getCardInfoErrCode
     * 
     * @return 卡号信息错误码描述语
     */
    public String getCardInfoErrDesc();

}
