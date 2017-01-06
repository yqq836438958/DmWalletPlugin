
package com.pacewear.httpserver;

import TRom.PayReqHead;
import com.qq.jce.wup.UniPacket;
import com.qq.taf.jce.JceStruct;

/**
 * @author baodingzhou
 */

public interface ITosService {

    public static final int ERR_DOCODE_ERROR = 10001;
    public static final int ERR_PARSE_ERROR = ERR_DOCODE_ERROR + 1;

    public static final String MODULE_NAME = "watchpay";

    public static final String MODULE_VER = "1.0";

    public static final String REQ_NAME = "req";

    public static final String RSP_NAME = "rsp";

    public static final int OPERTYPE_UNKNOWN = 2000;

    public static final int OPERTYPE_GET_PAY_BUSCARD_CONFIG_LIST = OPERTYPE_UNKNOWN + 1;

    public static final int OPERTYPE_GET_CARD_STATUS = OPERTYPE_GET_PAY_BUSCARD_CONFIG_LIST + 1;

    public static final int OPERTYPE_UNIFIED_ORDER = OPERTYPE_GET_CARD_STATUS + 1;

    public static final int OPERTYPE_GET_PAY_RESULT = OPERTYPE_UNIFIED_ORDER + 1;

    public static final int OPERTYPE_GET_BANK_CONFIG = OPERTYPE_GET_PAY_RESULT + 1;

    public static final int OPERTYPE_GET_BANK_CARD_LIST = OPERTYPE_GET_BANK_CONFIG + 1;

    public static final int OPERTYPE_VERIFY_BANK_CARD = OPERTYPE_GET_BANK_CARD_LIST + 1;

    public static final int OPERTYPE_BANK_PERSONALIZE = OPERTYPE_VERIFY_BANK_CARD + 1;

    public static final int OPERTYPE_BANK_REQUEST_OPT = OPERTYPE_BANK_PERSONALIZE + 1;

    public static final int OPERTYPE_BANK_VERIFY_OPT = OPERTYPE_BANK_REQUEST_OPT + 1;

    public static final int OPERTYPE_BANK_UNBIND = OPERTYPE_BANK_VERIFY_OPT + 1;

    public static final int OPERTYPE_CUSTOMERSUPPORT = OPERTYPE_BANK_UNBIND + 1;

    public static final int OPERTYPE_PULL_USER_INFO = OPERTYPE_CUSTOMERSUPPORT + 1;

    public static final int OPERTYPE_GET_VERIFYCODE = OPERTYPE_PULL_USER_INFO + 1;

    public static final int OPERTYPE_SEND_VERIFYCODE = OPERTYPE_GET_VERIFYCODE + 1;

    public static final int OPERTYPE_GET_PHONENUM = OPERTYPE_SEND_VERIFYCODE + 1;

    /**
     * getFunctionName
     * 
     * @return
     */
    public String getFunctionName();

    /**
     * getUniqueSeq
     * 
     * @return
     */
    public long getUniqueSeq();

    /**
     * getOperType
     * 
     * @return
     */
    public int getOperType();

    /**
     * getReq
     * 
     * @param payReqHead
     * @return
     */
    public JceStruct getReq(JceStruct payReqHead);

    /**
     * parse
     * 
     * @param packet
     * @return
     */
    public JceStruct parse(UniPacket packet);

    /**
     * getRspObject
     * 
     * @return
     */
    public JceStruct getRspObject();

    /**
     * invoke
     * 
     * @param listener
     * @return
     */
    public boolean invoke(IResponseObserver listener);
}
