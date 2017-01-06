
package com.pacewear.tsm.constants;

import android.net.Uri;

/**
 * 支付常量
 */
public interface PayConstants {

    /**
     * 定义通道数据重传次数
     */
    public static final int CHANNEL_RETRY_COUNT = 2;

    /**
     * 提醒缓存队列异常
     */
    public static final int NOTICE_BUFFER_SZIE = 50;

    /**
     * 当缓存队列过大可能会导致OOM，自动释放
     */
    public static final int AUTO_CLEAR_BUFFER_SIZE = 200;

    /**
     * 支付码Provider
     */
    public static final String PROVIDER_AUTHORITY = "com.tencent.tws.pay.PayCodeProvider";

    /**
     * 发送数据
     */
    public static final String PATH_SEND_DATA = "send_data";

    /**
     * 发送数据URI
     */
    public static final String URI_SEND_DATA = "content://" + PROVIDER_AUTHORITY + "/"
            + PATH_SEND_DATA;

    /**
     * 发送数据URI
     */
    public static final Uri CONTENT_URI_SEND_DATA = Uri.parse(URI_SEND_DATA);

    /**
     * 接收数据
     */
    public static final String PATH_RCV_DATA = "rcv_data";

    /**
     * 接收数据URI
     */
    public static final String URI_RCV_DATA = "content://" + PROVIDER_AUTHORITY + "/"
            + PATH_RCV_DATA;

    /**
     * 接收数据URI
     */
    public static final Uri CONTENT_URI_RCV_DATA = Uri.parse(URI_RCV_DATA);

    /**
     * 请求类型
     */
    public static final String QUERY_PARAMETER_BUSINESS_TYPE = "businessType";

    /**
     * 请求消息
     */
    public static final String QUERY_PARAMETER_REQ_DATA = "reqData";

    /**
     * Cursor Column 返回码
     */
    public static final String COLUMN_RET_CODE = "retCode";

    /**
     * Cursor Column 返回消息
     */
    public static final String COLUMN_RET_MSG = "retMsg";

    /**
     * Cursor Column 业务类型
     */
    public static final String COLUMN_BUSINESS_TYPE = "businessType";

    /**
     * Cursor Column 返回的业务数据
     */
    public static final String COLUMN_RET_DATA = "retData";

    public static final String[] COLUMNS_PAYMSG_RSP = new String[] {
            COLUMN_RET_CODE, COLUMN_RET_MSG, COLUMN_BUSINESS_TYPE, COLUMN_RET_DATA
    };

    /**
     * 请求支付码成功
     */
    public static final int RSP_OK = 0;

    /**
     * 设备未连接(蓝牙未连接)
     */
    public static final int RSP_DEVICE_DISCONNECTED = RSP_OK + 1;

    /**
     * 请求发送失败(透传回调提示失败)
     */
    public static final int RSP_REQ_SEND_FAILED = RSP_DEVICE_DISCONNECTED + 1;

    /**
     * 网络未连接(手机网络)
     */
    public static final int RSP_NETWORK_DISCONNECTED = RSP_REQ_SEND_FAILED + 1;

    /**
     * QQ未登录
     */
    public static final int RSP_NOT_LOGIN = RSP_NETWORK_DISCONNECTED + 1;

    /**
     * QQ未安装
     */
    public static final int RSP_QQ_UNINSTALL = RSP_NOT_LOGIN + 1;

    /**
     * QQ版本不支持
     */
    public static final int RSP_NOT_SUPPORT_VERSION = RSP_QQ_UNINSTALL + 1;

    /**
     * 未知错误(其它错误)
     */
    public static final int RSP_ERROR_UNKNOWN = RSP_NOT_SUPPORT_VERSION + 1;

}
