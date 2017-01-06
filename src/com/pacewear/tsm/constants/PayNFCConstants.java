
package com.pacewear.tsm.constants;


public interface PayNFCConstants {
	public static final String TAG = "NFC";
	
	// 模块版本号 1.0, 1.1, 1.2 ...
	// 与Server协议交互时请求头参数
	public static final String MODULE_VER = "1.0";
	
	public static final String APP_ID_FOR_QQPAY = "1105222839";

    // Wallet 同步消息
    // TODO 变更类型，aid
    public static final String ACTION_WALLET_SYNC_MSG_FROM_WATCH = "action.tws.wallet.sync.msg.from.watch";
    public static final String ACTION_WALLET_SYNC_MSG_FROM_PHONE = "action.tws.wallet.sync.msg.from.phone";

	// 商户号
	public static final String APP_PARTNER_ID = "1800001531";
	public static final String QQPAY_CALLBACK_SCHEME = "qwallet1800001531"; 
	public static final String ACTION_WXPAY_RESULT_NOTIFY = "action.wxpay.result.notify";
	public static final String ACTION_QQPAY_RESULT_NOTIFY = "action.qqpay.result.notify";

	public static final String RC_BANKCARD_OPT_SUC = "0000";
	
	public static final String DEVICE_TYPE = "watch";
	
	public static final String CHARSET_UFT8 = "UTF8";
	
	public static final String TRADE_TYPE = "APP";
	
	// 包名，雪球接口参数需要
	public static final String PACKAGE_NAME_WALLET = "com.tencent.tws.wallet";
	// 品牌名
	public static final String BRAND_NAME = "Tencent";
	// 型号
	public static final String MACHINE_TYPE = "Taishan001";
	
	public interface CMD {
		public static final int CMD_WATCH_LOADINSTALL = 1000; 						// 开卡，下载Applet安装脚本
		public static final int CMD_WATCH_CARDPERSONAL = CMD_WATCH_LOADINSTALL+1;		// 开卡，卡片个人化
		public static final int CMD_WATCH_CARDQUERY = CMD_WATCH_CARDPERSONAL+1;			// 单卡片查询
		public static final int CMD_WATCH_CARDTOPUP = CMD_WATCH_CARDQUERY+1;			// 卡片圈存，充值
		public static final int CMD_WATCH_CARDLISTQUERY = CMD_WATCH_CARDTOPUP+1; 		// 卡列表查询
		public static final int CMD_WATCH_CARDSWITCH = CMD_WATCH_CARDLISTQUERY+1; 		// 默认卡片切换
		
		public static final int CMD_SERVER_GETORDER = 2000;						// 获取订单
		public static final int CMD_SERVER_GETBUSCARDPAYCONFIG = CMD_SERVER_GETORDER + 1;
		public static final int CMD_SERVER_GETTOKEN = CMD_SERVER_GETBUSCARDPAYCONFIG+1;	// 获取Token
		public static final int CMD_SERVER_GETPAYRESULT = CMD_SERVER_GETTOKEN+1;// 获取支付结果
		public static final int CMD_SERVER_APPLYFUND = CMD_SERVER_GETPAYRESULT+1;// 申请退款
		public static final int CMD_SERVER_DATAREPORT = CMD_SERVER_APPLYFUND+1;// 数据上报
		public static final int CMD_SERVER_GETCARDSTATUS = CMD_SERVER_DATAREPORT+1;// 获取上一次开卡状态
	}

	public interface JsonProperty {
		public static final String PROPERTY_CARD_LIST = "card_list";
		public static final String PROPERTY_INSTANCE_ID = "instance_id";
		public static final String PROPERTY_INSTALL_STATUS = "install_status";
		public static final String PROPERTY_ACTIVATION_STATUS = "activation_status";
		public static final String PROPERTY_CARD_BALANCE = "card_balance";
		public static final String PROPERTY_CARD_NUMBER = "card_number";
		
		public static final String PROPERTY_BANKCARD_CPLC = "x-snbps-cplc";
		public static final String PROPERTY_BANKCARD_UMID = "x-snbps-umid";
		public static final String PROPERTY_BANKCARD_DEVICE = "x-snbps-device";
		public static final String PROPERTY_BANKCARD_DMID = "x-snbps-dmid";
		public static final String PROPERTY_BANKCARD_RESULTCODE = "resultCode";
		public static final String PROPERTY_BANKCARD_RESULTMSG = "ResultMsg";
		public static final String PROPERTY_BANKCARD_SESSIONID = "sessionId";
		public static final String PROPERTY_BANKCARD_XSESSIONID = "x-snbps-sessionid";
		public static final String PROPERTY_BANKCARD_TERMSID = "termsId";
		public static final String PROPERTY_BANKCARD_CIPHERDATA = "cipherData";
		public static final String PROPERTY_BANKCARD_VCARDREFID = "vertualCardReferenceId";
		public static final String PROPERTY_BANKCARD_VCARDNUM = "virtualCardNumber";
		public static final String PROPERTY_BANKCARD_EXPDATE = "expDate";
		public static final String PROPERTY_BANKCARD_LASTDIGITS = "lastDigits";
		public static final String PROPERTY_BANKCARD_CARD_PRODUCT_TYPEID = "cardProducttypeId";
		public static final String PROPERTY_BANKCARD_CARDREFID = "cardReferenceId";
		public static final String PROPERTY_BANKCARD_INSTANCE_AID = "instanceAid";
		public static final String PROPERTY_BANKCARD_OTPMETHOD = "otpMethod";
		public static final String PROPERTY_BANKCARD_OTPVALUE = "otpValue";
		public static final String PROPERTY_BANKCARD_CARDTYPE = "cardType";
		public static final String PROPERTY_BANKCARD_ACTIONTYPE = "actionType";
	}
	
	public interface Card {
		public static final int TYPE_UNKNOWN = -1;
		public static final int TYPE_TRAFFIC = 0;
		public static final int TYPE_BANK = 1;
		
		public static final int SUBTYPE_BANK_DEBIT = 1;
		public static final int SUBTYPE_BANK_CREDIT = 2;
		public static final int SUBTYPE_BANK_WUXIAN = 3;
		
		public static final String INSTANCE_ID_BEIJING = "9156000014010001";
		public static final String INSTANCE_ID_SHENZHEN = "535A542E57414C4C45542E454E56";
		public static final String INSTANCE_ID_LINGNAN = "5943542E55534552";
		public static final String INSTANCE_ID_CMB_DEBIT = "315041592E5359532E4444463033";
		public static final String INSTANCE_ID_CMB_CREDIT = "315041592E5359532E4444463034";
		public static final String INSTANCE_ID_CMB_WUXIAN = "315041592E5359532E4444463035";
		
		public static final String CARD_NUMBER_DEF = "00000000";
	}

	public interface TrafficCardType {
		public static final int TYPE_FIRST = 1;
		
		public static final int BEIGING_YIKATONG = TYPE_FIRST;
		public static final int SHANGHAI_JIAOTONGKA = BEIGING_YIKATONG+1;
		public static final int GUANGZHOU_LINGNANTONG = SHANGHAI_JIAOTONGKA+1;
		public static final int SHENZHEN_TONG = GUANGZHOU_LINGNANTONG+1;
		public static final int SHENZHEN_QIANHAITONG = SHENZHEN_TONG+1;
		
		public static final int TYPE_LAST = SHENZHEN_QIANHAITONG;
	}
	
	public interface BankCardType {
		public static final int TYPE_FIRST = 1;
		
		public static final int CMB = TYPE_FIRST;
		
		public static final int TYPE_LAST = CMB;
	}
	
	public interface ExtraKeyName{
		public static final String EXTRA_INT_NFCEVENT = "EXTRA_INT_NFCEVENT";
		public static final String EXTRA_INT_NFCEVENTRSPCODE = "EXTRA_INT_NFCEVENTRSPCODE";
		public static final String EXTRA_STR_WATCH_RESPONSE = "EXTRA_STR_WATCH_RESPONSE";
		public static final String EXTRA_STR_TRADE_NO = "EXTRA_STR_TRADE_NO";
		public static final String EXTRA_STR_CPLC = "EXTRA_STR_CPLC";
		public static final String EXTRA_STR_TOKEN = "EXTRA_STR_TOKEN";
		public static final String EXTRA_STR_TOKEN_RSPCODE = "EXTRA_STR_TOKEN_RSPCODE";
		public static final String EXTRA_INT_CARDTYPE = "EXTRA_INT_CARDTYPE";
		public static final String EXTRA_INT_CARDINDEX = "EXTRA_INT_CARDINDEX";
		public static final String EXTRA_INT_CARDSUBTYPE = "EXTRA_INT_CARDSUBTYPE";
		public static final String EXTRA_STR_CARD_NUMBER = "EXTRA_STR_CARD_NUMBER";
		public static final String EXTRA_STR_CARD_ISSUENAME = "EXTRA_STR_CARD_ISSUENAME";
		public static final String EXTRA_STR_TRADE_NO_SERVER = "EXTRA_STR_TRADE_NO_SERVER";
		public static final String EXTRA_STR_TRADE_NO_THREE_PARTY = "EXTRA_STR_TRADE_NO_THREE_PARTY";
		public static final String EXTRA_STR_INSTANCE_ID = "EXTRA_STR_INSTANCE_ID";
		public static final String EXTRA_INT_PAY_TYPE = "EXTRA_INT_PAY_TYPE";
		public static final String EXTRA_INT_PAY_SCENE = "EXTRA_INT_PAY_SCENE";
		public static final String EXTRA_STR_GOODS_NAME = "EXTRA_STR_GOODS_NAME";
		public static final String EXTRA_STR_GOODS_DETAIL = "EXTRA_STR_GOODS_DETAIL";
		public static final String EXTRA_LNG_TOTAL_FEE = "EXTRA_LONG_TOTAL_FEE";
		public static final String EXTRA_LNG_OPENCARD_FEE = "EXTRA_LONG_OPENCARD_FEE";
		public static final String EXTRA_STR_TIMESTAMP = "EXTRA_STR_TIMESTAMP";
		public static final String EXTRA_STR_PAYSIGN = "EXTRA_STR_PAYSIGN";
		public static final String EXTRA_STR_ISSUER_ID = "EXTRA_STR_ISSUER_ID";
		public static final String EXTRA_STR_ISSUER_NAME = "EXTRA_STR_ISSUER_NAME";
		public static final String EXTRA_STR_USERTERMS_ID = "EXTRA_STR_USERTERMS_ID";
		public static final String EXTRA_STR_USERTERMS_CONTENT = "EXTRA_STR_USERTERMS_CONTENT";
		public static final String EXTRA_BOOL_IS_RETRY = "EXTRA_BOOL_IS_RETRY";
		public static final String EXTRA_BOOL_SYNC_LAST_STATUS = "EXTRA_BOOL_SYNC_LAST_STATUS";
		
		public static final String EXTRA_STR_APPID = "EXTRA_STR_APPID";
		public static final String EXTRA_STR_PARTNERID = "EXTRA_STR_PARTNERID";
		public static final String EXTRA_STR_PREPAYID = "EXTRA_STR_PREPAYID";
		public static final String EXTRA_STR_NONCESTR = "EXTRA_STR_NONCESTR";
		public static final String EXTRA_STR_ORDER_PACKAGE = "EXTRA_STR_ORDER_PACKAGE";
		public static final String EXTRA_STR_ORDER_SIGN = "EXTRA_STR_ORDER_SIGN";
		public static final String EXTRA_STR_ORDER_RETCODE = "EXTRA_STR_ORDER_RETCODE";
		
		public static final String EXTRA_INT_BANKCARD_OPT = "EXTRA_INT_BANKCARD_OPT";
		public static final String EXTRA_STR_BANKCARD_VIRTUAL_CARDREFID = "EXTRA_STR_BANKCARD_VIRTUAL_CARDREFID";
		public static final String EXTRA_STR_BANKCARD_VIRTURAL_CARDNUM = "EXTRA_STR_BANKCARD_VIRTURAL_CARDNUM";
		public static final String EXTRA_STR_BANKCARD_SESSIONID = "EXTRA_STR_BANKCARD_SESSIONID";
		public static final String EXTRA_STR_BANKCARD_TERMSID = "EXTRA_STR_BANKCARD_TERMSID";
		public static final String EXTRA_STR_BANKCARD_CIPHERDATA = "EXTRA_STR_BANKCARD_CIPHERDATA";
		public static final String EXTRA_STR_BANKCARD_EXPDATE = "EXTRA_STR_BANKCARD_EXPDATE";
		public static final String EXTRA_STR_BANKCARD_LASTDIGITS = "EXTRA_STR_BANKCARD_LASTDIGITS";
		public static final String EXTRA_STR_BANKCARD_CARD_PRODUCT_TYPEID = "EXTRA_STR_BANKCARD_CARD_PRODUCT_TYPEID";
		public static final String EXTRA_STR_BANKCARD_CARD_REFID = "EXTRA_STR_BANKCARD_CARD_REFID";
		public static final String EXTRA_STR_BANKCARD_OTPVALUE = "EXTRA_STR_BANKCARD_OTPVALUE";
		public static final String EXTRA_STR_BANKCARD_ACTIONTYPE = "EXTRA_STR_BANKCARD_ACTIONTYPE";

	}
	
	// UI层调用底层回调事件
	public interface NFCEvent {
		public static final int EVENT_SYNCDATA = 1; // 打开卡包，同步数据
	    public static final int EVENT_GET_BUSCARDPAYCONFIG = EVENT_SYNCDATA + 1;   // 开卡、充值配置
		public static final int EVENT_OPENCARD = EVENT_GET_BUSCARDPAYCONFIG + 1;   	// 开卡
		public static final int EVENT_CARDTOPUP = EVENT_OPENCARD+1;  		// 圈存，充值
		public static final int EVENT_CARDLISTQUERY = EVENT_CARDTOPUP+1; 	// 卡列表查询
		public static final int EVENT_CARDQUERY = EVENT_CARDLISTQUERY+1; 		// 单卡查询，同步手表设备卡列表完成
		public static final int EVENT_CARDSWITCH = EVENT_CARDQUERY+1; 		// 默认卡片切换 
		public static final int EVENT_APPLYFUND = EVENT_CARDSWITCH+1;		// 申请退款
		public static final int EVENT_DATAREPORT = EVENT_APPLYFUND+1;
		public static final int EVENT_GET_CARDSTATUS = EVENT_DATAREPORT+1;
		public static final int EVENT_INIT_SE = EVENT_GET_CARDSTATUS+1;
	}
	
	public interface NFCEventRspCode {
		public static final int RC_UNKNOWN = -7000;
		public static final int RC_OK = 0;
		public static final int RC_GETORDER_FAIL = 1;
		public static final int RC_GETTOKEN_FAIL = 2;
		public static final int RC_PAY_FAIL = 3;
		public static final int RC_DEVICE_NOT_CONNECTED = 4;
		public static final int RC_GETPAYRESULT_FAIL = 5;
		public static final int RC_APPLYREFUND_FAIL = 6;
		public static final int RC_UNSUPPORT_CMD = 7;
		public static final int RC_GETUSERTERMS_FAIL = 8;
		public static final int RC_GETBANKCARDS_FAIL = 9;
		public static final int RC_SERVER_FAIL = 10;
		public static final int RC_SERVER_RSP_NULL = 11;
		public static final int RC_UPDATE_FAIL = 12;
		public static final int RC_SERVER_RSP_PARSE_EXP = 13;
		public static final int RC_LOCAL_EXCEPTION = 14;
		public static final int RC_SEND_REQ_FAIL = 15;
		public static final int RC_WATCH_RSP_NULL = 16;
		public static final int RC_WATCH_REQ_TIMEOUT = 17;
		public static final int RC_SYNC_LAST_CARDSTATUS_FAIL = 18;
		public static final int RC_LOCAL_DATA_EXP = 19;
	}
	
	public interface WUP {
		public static final String MODULE_NAME = "watchpay";
		public static final String MODULE_NAME_TSM = "tsmapi";
		public static final String REQ_NAME = "req";
		public static final String REQ_NAME_TSM = "stReq";
		public static final String RSP_NAME = "rsp";
		public static final String RSP_NAME_TSM = "stRsp";

		public static final String FUNCTION_NAME_GET_BUS_CARD_PAYCONFIG = "getPayBusCardConfigList";
		public static final String FUNCTION_NAME_GET_ORDER = "unifiedOrder";
		public static final String FUNCTION_NAME_GET_PAYRESULT = "getPayResult";
		public static final String FUNCTION_NAME_GET_APPLYREFUND = "applyRefund";
		public static final String FUNCTION_NAME_SEND_CMD = "sendCmd";
		public static final String FUNCTION_NAME_DATAREPORT = "reportOpenBusCardRecord";
		public static final String FUNCTION_NAME_GETCARDSTATUS = "getLatestCardStatus";
	}
	
	public interface OTPMethod {
		public static final String SMS = "SMS";
		public static final String TEL = "TEL";
		public static final String EMAIL = "EMAIL";
	}
	
	public interface BankCardAction {
		public static final int INSTALL = 1;
		public static final int LOCK = 2;
		public static final int UNLOCK = 3;
		public static final int DELETE = 4;
		public static final int LOAD = 5;
	}
}
