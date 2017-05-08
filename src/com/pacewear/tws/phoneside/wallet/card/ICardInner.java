
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
                R.drawable.panel_card_shenzhen,
                // Card Balance TextColor
                // TODO
                R.drawable.panel_card_shenzhen_failed,
                // Card Bg
                R.drawable.panel_card_shenzhen_little,
                // Card Balance TextColor
                // TODO
                R.drawable.panel_card_shenzhen_little_failed,
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
                R.drawable.panel_card_beijing,
                // Card Balance TextColor
                R.drawable.panel_card_beijing_failed,
                // Card Bg
                R.drawable.panel_card_beijing_little,
                // Card Balance TextColor
                // TODO
                R.drawable.panel_card_beijing_little_failed,
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
                R.drawable.panel_card_lingnantong,
                // Card Balance TextColor
                R.drawable.panel_card_lingnantong_failed,
                R.drawable.panel_card_lingnantong_little,
                // Card Balance TextColor
                // TODO
                R.drawable.panel_card_lingnantong_little_failed,
                // class
                TrafficCard.class),;

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

        public int mCardDisableRes = 0;

        public int mCardLiteBgRes = 0;

        public int mCardLiteDisableRes = 0;

        private CONFIG(String aid, CARD_TYPE type, int cardNameRes, int cardIconRes,
                int cardBgRes, int cardDisableBgRes, int cardLiteBgRes, int cardDisableLiteBgRes,
                Class<? extends Card> cls) {
            mAID = aid;
            mType = type;
            mCardNameRes = cardNameRes;
            mCardIconRes = cardIconRes;
            mCardBgRes = cardBgRes;
            mCardDisableRes = cardDisableBgRes;
            mCardLiteBgRes = cardLiteBgRes;
            mCardLiteDisableRes = cardDisableLiteBgRes;
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
