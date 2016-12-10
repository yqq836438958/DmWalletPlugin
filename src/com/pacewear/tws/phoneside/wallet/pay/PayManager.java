
package com.pacewear.tws.phoneside.wallet.pay;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;

import com.pacewear.tws.phoneside.wallet.common.Constants;
import com.pacewear.tws.phoneside.wallet.common.SeqGenerator;
import com.pacewear.tws.phoneside.wallet.common.Utils;
import com.pacewear.tws.phoneside.wallet.order.IOrderInner;
import com.pacewear.tws.phoneside.wallet.order.IOrderManagerInner;
import com.pacewear.tws.phoneside.wallet.order.OrderManager;
import com.tencent.mm.sdk.modelpay.PayReq;
import com.tencent.mm.sdk.modelpay.PayResp;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mobileqq.openpay.api.IOpenApi;
import com.tencent.mobileqq.openpay.api.OpenApiFactory;
import com.tencent.mobileqq.openpay.data.pay.PayApi;
import com.tencent.mobileqq.openpay.data.pay.PayResponse;
import com.tencent.tws.devicemanager.AppInfoProvider;
import com.tencent.tws.framework.global.GlobalObj;
import com.tencent.tws.phoneside.business.AccountManager;
import com.tencent.utils.DeviceUtils;

import qrom.component.log.QRomLog;

import java.util.HashMap;

import TRom.E_PAY_TYPE;
import TRom.OrderReqParam;
import TRom.OrderRspParam;

/**
 * @author baodingzhou
 */

public class PayManager implements IPayManagerInner {

    private static final String TAG = "PayManager";

    private static volatile IPayManagerInner sInstance = null;

    private IOpenApi openApi;

    private IWXAPI miwxApi;

    private BroadcastReceiver mBroadcastReceiver;

    private HashMap<String, String> mTradeNoMap = new HashMap<String, String>();

    private Handler mHandler = new Handler(Utils.getWorkerlooper());

    private boolean isPaying = false;

    private PayManager() {
        initPayResultReceiver();

        openApi = OpenApiFactory.getInstance(GlobalObj.g_appContext, Constants.APP_ID_FOR_QQPAY);
        miwxApi = AccountManager.getInstance().getWXApi();
    }

    public static IPayManagerInner getInstanceInner() {
        if (sInstance == null) {
            synchronized (PayManager.class) {
                if (sInstance == null) {
                    sInstance = new PayManager();
                }
            }
        }

        return sInstance;
    }

    @Override
    public boolean pay(String tradeNo) {
        QRomLog.d(TAG, "pay for tradeNo:" + tradeNo);
        boolean handled = false;

        IOrderManagerInner orderManagerInner = OrderManager.getInstanceInner();
        IOrderInner order = orderManagerInner.getOrderInner(tradeNo);

        if (order == null) {
            QRomLog.e(TAG, "can not get order for tradeNo:" + tradeNo);
            return handled;
        }

        OrderReqParam orderReqParam = order.getOrderReqParam();
        OrderRspParam orderRspParam = order.getOrderRspParam();

        if (orderReqParam == null || orderRspParam == null) {
            QRomLog.e(TAG, "Order pay params error");
            return handled;
        }

        switch (orderReqParam.ePayType) {
            case E_PAY_TYPE._E_PT_QQ_PAY:
                handled = doQQPay(order);
                break;
            case E_PAY_TYPE._E_PT_WEIXIN_PAY:
                handled = doWeChatPay(order);
                break;
            default:
                break;
        }

        return handled;
    }

    private boolean doQQPay(IOrderInner order) {
        QRomLog.d(TAG, "doQQPay");
        OrderRspParam orderRspParam = order.getOrderRspParam();
        String prepayId = orderRspParam.getSPayid();
        if (TextUtils.isEmpty(prepayId)) {
            QRomLog.e(TAG, "prepayId is empty");
            return false;
        }

        PayApi api = new PayApi();
        api.appId = orderRspParam.getSAppId();

        api.serialNumber = "" + SeqGenerator.getInstance().uniqueSeq();
        api.callbackScheme = Constants.QQPAY_CALLBACK_SCHEME;
        api.tokenId = orderRspParam.getSPayid();
        api.pubAcc = "";
        api.pubAccHint = "";
        api.nonce = orderRspParam.getSNoncestr();
        api.timeStamp = System.currentTimeMillis() / 1000;
        api.bargainorId = orderRspParam.getSpartnerid();
        api.sig = orderRspParam.getSSign();
        api.sigType = orderRspParam.getSSignType();

        if (api.checkParams()) {
            if (openApi.execApi(api)) {
                mTradeNoMap.put(api.serialNumber, order.getOrderRspParam().sTradeNo);
                isPaying = true;
                return true;
            } else {
                QRomLog.e(TAG, "execApi error");
            }
        } else {
            QRomLog.d(TAG, "checkParams error");
        }
        return false;
    }

    private boolean doWeChatPay(IOrderInner order) {
        QRomLog.d(TAG, "doWeChatPay");
        OrderRspParam orderRspParam = order.getOrderRspParam();
        String prepayId = orderRspParam.getSPayid();
        if (TextUtils.isEmpty(prepayId)) {
            QRomLog.e(TAG, "prepayId is empty");
            return false;
        }

        PayReq req = new PayReq();
        req.appId = orderRspParam.getSAppId();
        req.partnerId = orderRspParam.getSpartnerid();
        req.prepayId = orderRspParam.getSPayid();
        req.nonceStr = orderRspParam.getSNoncestr();
        req.timeStamp = orderRspParam.getSTimestamp();
        req.packageValue = orderRspParam.getSPackage();
        req.sign = orderRspParam.getSSign();

        if (miwxApi.sendReq(req)) {
            isPaying = true;
            mTradeNoMap.put(prepayId, order.getOrderRspParam().sTradeNo);
            return true;
        } else {
            QRomLog.e(TAG, "sendReq error");
        }
        return false;
    }

    private void initPayResultReceiver() {

        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, final Intent intent) {

                QRomLog.d(TAG, "onReceive " + intent.getAction());

                if (intent == null || intent.getExtras() == null) {
                    return;
                }

                if (Constants.ACTION_QQPAY_RESULT_NOTIFY.equals(intent.getAction())) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            onQQPayResult(intent);
                        }
                    });

                } else if (Constants.ACTION_WXPAY_RESULT_NOTIFY.equals(intent.getAction())) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            onWXPayResult(intent);
                        }
                    });

                }
            }

            private void onQQPayResult(Intent intent) {
                QRomLog.d(TAG, "onQQPayResult");
                PayResponse rsp = new PayResponse();
                rsp.fromBundle(intent.getExtras());

                String tradeNo = mTradeNoMap.remove(rsp.serialNumber);
                isPaying = false;
                if (tradeNo == null) {
                    QRomLog.d(TAG, "can not get tradeNo for " + rsp.serialNumber);
                    // TODO
                    return;
                }

                if (rsp.isSuccess()) {
                    OrderManager.getInstanceInner().setOrderLocalPaidStatus(tradeNo, true);
                } else {
                    OrderManager.getInstanceInner().setOrderLocalPaidStatus(tradeNo, false);
                }
            }

            private void onWXPayResult(Intent intent) {
                PayResp rsp = new PayResp(intent.getExtras());

                String tradeNo = mTradeNoMap.remove(rsp.prepayId);
                isPaying = false;
                if (tradeNo == null) {
                    // TODO
                    return;
                }

                if (rsp.errCode == 0) {
                    OrderManager.getInstanceInner().setOrderLocalPaidStatus(tradeNo, true);
                } else {
                    OrderManager.getInstanceInner().setOrderLocalPaidStatus(tradeNo, false);
                }
            }
        };

        IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.ACTION_QQPAY_RESULT_NOTIFY);
        filter.addAction(Constants.ACTION_WXPAY_RESULT_NOTIFY);
        LocalBroadcastManager.getInstance(GlobalObj.g_appContext).registerReceiver(
                mBroadcastReceiver, filter);
    }

    public static boolean isPayChannelSupport(int channel) {
        String packagename = "";
        switch (channel) {
            case E_PAY_TYPE._E_PT_WEIXIN_PAY:
                packagename = AppInfoProvider.PKG_WECHAT;
                break;
            case E_PAY_TYPE._E_PT_QQ_PAY:
                packagename = AppInfoProvider.PKG_QQ;
                break;
            default:
                break;
        }
        if ("".equals(packagename)) {
            return false;
        }
        return DeviceUtils.checkAppInstalled(GlobalObj.g_appContext, packagename);
    }

    @Override
    public boolean isPaying() {
        return isPaying;
    }

    @Override
    public void cancelPay() {
        isPaying = false;
    }
}
