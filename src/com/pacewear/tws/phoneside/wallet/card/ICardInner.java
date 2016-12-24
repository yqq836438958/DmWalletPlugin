
package com.pacewear.tws.phoneside.wallet.card;

import com.pacewear.tws.phoneside.wallet.R;

import qrom.component.log.QRomLog;

/**
 * @author baodingzhou
 */

public interface ICardInner extends ICard {

    /**
     * 卡基本配置
     */
    public enum CONFIG {

        /**
         * 深圳通
         */
        SHENZHENTONG(
                // AID
                "535A542E57414C4C45542E454E56",
                // Card Type
                CARD_TYPE.TRAFFIC_CARD,
                // Card Name Res
                R.string.nfc_traffic_card_shenzhen,
                // Card ICon
                R.drawable.wallet_ic_shenzhen,
                // Card Bg
                R.drawable.wallet_szt_card_bg,
                // Card Balance TextColor
                R.color.wallet_szt_balance,
                // Card Balance Unit Icon
                R.drawable.wallet_ic_balance_gray,
                // class
                TrafficCard.class),

        /**
         * 北京通
         */
        BEIJINGTONG(
                // AID
                "9156000014010001",
                // Card Type
                CARD_TYPE.TRAFFIC_CARD,
                // Card Name Res
                R.string.nfc_traffic_card_beijing,
                // Card ICon
                R.drawable.wallet_ic_beijing,
                // Card Bg
                R.drawable.wallet_bjt_card_bg,
                // Card Balance TextColor
                R.color.wallet_bjt_balance,
                // Card Balance Unit Icon
                R.drawable.wallet_ic_balance,
                // class
                TrafficCard.class),

        /**
         * 岭南通
         */
        LINGNANTONG(
                // AID
                "5943542E55534552",
                // Card Type
                CARD_TYPE.TRAFFIC_CARD,
                // Card Name Res
                R.string.nfc_traffic_card_lingnan,
                // Card ICon
                R.drawable.wallet_ic_lingnan,
                // Card Bg
                R.drawable.wallet_lnt_card_bg,
                // Card Balance TextColor
                R.color.wallet_lnt_balance,
                // Card Balance Unit Icon
                R.drawable.wallet_ic_balance,
                // class
                TrafficCard.class),
        ;

        private static final String TAG = "ICard.CONFIG";

        /**
         * Class
         */
        private Class<? extends Card> mClass = null;

        /**
         * AID
         */
        public String mAID = "";

        /**
         * 卡类型
         */
        public CARD_TYPE mType = CARD_TYPE.TRAFFIC_CARD;

        public int mCardNameRes = 0;
        
        public int mCardIconRes = 0;
        
        public int mCardBgRes = 0;
        
        public int mBalanceTxtColor = 0;

        public int mBalanceIcon = 0;

        private CONFIG(String aid, CARD_TYPE type, int cardNameRes,int cardIconRes,
                int cardBgRes,int balanceTxtColor,int balanceIcon,Class<? extends Card> cls) {
            mAID = aid;
            mType = type;
            mCardNameRes = cardNameRes;
            mCardIconRes = cardIconRes;
            mCardBgRes = cardBgRes;
            mBalanceTxtColor = balanceTxtColor;
            mBalanceIcon = balanceIcon;
            mClass = cls;
        }

        /**
         * newInstance
         * 
         * @return
         */
        public ICardInner newInstance() {

            ICardInner card = null;

            try {
                card = (ICardInner) mClass.newInstance();
            } catch (InstantiationException e) {
                QRomLog.e(TAG, e.getMessage());
            } catch (IllegalAccessException e) {
                QRomLog.e(TAG, e.getMessage());
            }

            if (card != null) {
                card.setConfig(this);
            }

            return card;
        }
    }

    /**
     * setConfig
     * 
     * @param config
     */
    public void setConfig(CONFIG config);

    /**
     * setInstallStatus
     * 
     * @param installStatus
     */
    public void setInstallStatus(INSTALL_STATUS installStatus);

    /**
     * setActivationStatus
     * 
     * @param activationStatus
     */
    public void setActivationStatus(ACTIVATION_STATUS activationStatus);
}
