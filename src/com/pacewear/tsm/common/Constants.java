
package com.pacewear.tsm.common;

/**
 * @author baodingzhou
 */

public class Constants {
    public static final String UTF8 = "UTF8";
    public static final String APP_ID_FOR_QQPAY = "1105222839";
    public static final String APP_PARTNER_ID = "1800001531";
    public static final String QQPAY_CALLBACK_SCHEME = "qwallet1800001531";
    public static final String ACTION_WXPAY_RESULT_NOTIFY = "action.wxpay.result.notify";
    public static final String ACTION_QQPAY_RESULT_NOTIFY = "action.qqpay.result.notify";

    public static final String TRADE_TYPE = "APP";
    public static final String WALLET_PACKAGE_NAME = "com.tencent.tws.wallet";
    public static final int WALLET_ERRCODE_EMPTYLIST = 400301;
    public static final int WALLET_ACCOUNT_AUTH_FAILED = -101;
    public static final int WALLET_QUERY_MAX_TIMES = 3;
    public static final long WALLET_ORDER_SYNC_DURATION = 10 * 1000;
    public static final long WALLET_CONFIG_SYNC_DURATION = 5 * 60 * 1000;
    public static final String WALLET_ERR_PLATFORM_SNOWBALL = "01";
    public static final String WALLET_ERR_PLATFORM_BEIJING = "02";
    public static final String WALLET_DEFAULT_CITYCODE = "4401";
    public static final String WALLET_DEFAULT_CITYCODE_GZ = "01";
    public static final String TSM_DEFAULT_CARDMAIN_AID = "A000000151000000";
    public static final String TSM_CRS_AID = "A00000015143525300";
    public static final String TSM_KEY_AID = "instance_id";
    public static final String TSM_KEY_APP_STAT = "instance_install";
    public static final String TSM_KEY_APP_SELECT = "instance_active";
}
