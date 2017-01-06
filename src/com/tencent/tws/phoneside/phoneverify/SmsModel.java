
package com.tencent.tws.phoneside.phoneverify;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.text.TextUtils;

import com.pacewear.httpserver.IResponseObserver;
import com.pacewear.tws.phoneside.wallet.WalletApp;
import com.qq.taf.jce.JceStruct;
import com.tencent.tws.phoneside.business.AccountManager;

import java.util.HashMap;

import TRom.PhoneNumberRsp;
import TRom.RomAccountInfo;
import TRom.SecurityCodeRsp;

public class SmsModel {
    private static SmsModel sInstance = null;
    private HashMap<String, String> mPhoneNumMap = new HashMap<String, String>();
    private static String CACHEFILE = "sms_cache";

    public static interface OnSmsCallback {
        public void OnResult(int ret, String result);
    }

    public static SmsModel get() {
        if (sInstance == null) {
            synchronized (SmsModel.class) {
                sInstance = new SmsModel();
            }
        }
        return sInstance;
    }

    public void getSecurityCode(String phone, OnSmsCallback callback) {
        GetSecurityCode getSecurityCode = new GetSecurityCode();
        getSecurityCode.setParam(phone, null);
        sendTosServiceReq(phone, getSecurityCode, callback);
    }

    public void sendSecurityCode(String phone, String verifyCode, OnSmsCallback callback) {
        SendSecurityCode sendSecurityCode = new SendSecurityCode();
        sendSecurityCode.setParam(phone, verifyCode);
        sendTosServiceReq(phone, sendSecurityCode, callback);
    }

    public void getPhoneNum(final OnSmsCallback callback) {
        String tmpPhone = getPhoneNum();
        if (!TextUtils.isEmpty(tmpPhone)) {
            postResult(callback, 0, tmpPhone);
            return;
        }
        GetPhoneNumber getPhoneNumber = new GetPhoneNumber();
        final long lSeq = getPhoneNumber.getUniqueSeq();
        getPhoneNumber.invoke(new IResponseObserver() {

            @Override
            public void onResponseSucceed(long uniqueSeq, int operType, JceStruct response) {
                if (lSeq == uniqueSeq) {
                    PhoneNumberRsp data = (PhoneNumberRsp) response;
                    if (data == null) {
                        postResult(callback, -1, null);
                        return;
                    }
                    if (data.iRet != 0) {
                        postResult(callback, data.iRet, null);
                        return;
                    }
                    if (TextUtils.isEmpty(data.sPhoneNum)) {
                        postResult(callback, -1, null);
                        return;
                    }
                    savePhoneNum(data.sPhoneNum);
                    postResult(callback, 0, data.sPhoneNum);
                }

            }

            @Override
            public void onResponseFailed(long uniqueSeq, int operType, int errorCode,
                    String description) {
                if (lSeq == uniqueSeq) {
                    postResult(callback, errorCode, description);
                }
            }
        });
    }

    private void sendTosServiceReq(final String phone, final SmsTosService service,
            final OnSmsCallback callback) {
        final long lSeq = service.getUniqueSeq();
        service.invoke(new IResponseObserver() {

            @Override
            public void onResponseSucceed(long uniqueSeq, int operType, JceStruct response) {
                if (lSeq == uniqueSeq) {
                    SecurityCodeRsp data = (SecurityCodeRsp) response;
                    if (data == null) {
                        postResult(callback, -1, null);
                        return;
                    }
                    if (data.iRet != 0) {
                        postResult(callback, data.iRet, null);
                        return;
                    }
                    if (service instanceof SendSecurityCode) {
                        savePhoneNum(phone);
                    }
                    postResult(callback, 0, "");
                }

            }

            @Override
            public void onResponseFailed(long uniqueSeq, int operType, int errorCode,
                    String description) {
                if (lSeq == uniqueSeq) {
                    postResult(callback, errorCode, description);
                }
            }
        });
    }

    private void postResult(OnSmsCallback callback, int ret, String result) {
        if (callback == null) {
            return;
        }
        callback.OnResult(ret, result);
    }

    public String getPhoneNum() {
        RomAccountInfo account = AccountManager.getInstance().getLoginAccountIdInfo();
        if (account == null || TextUtils.isEmpty(account.getSAccount())) {
            return "";
        }
        String key = new StringBuilder(account.getSAccount()).append("-")
                .append(account.getERomAccountType()).toString();
        String tmpVal = mPhoneNumMap.get(key);
        if (!TextUtils.isEmpty(tmpVal)) {
            return tmpVal;
        }
        SharedPreferences sharedPreferences = WalletApp.sGlobalCtx.getSharedPreferences(CACHEFILE,
                0);
        String phone = sharedPreferences.getString(key, "");
        if (TextUtils.isEmpty(phone)) {
            return phone;
        }
        mPhoneNumMap.put(key, phone);
        return phone;
    }

    private void savePhoneNum(String phone) {
        RomAccountInfo account = AccountManager.getInstance().getLoginAccountIdInfo();
        if (account == null || TextUtils.isEmpty(account.getSAccount())) {
            return;
        }
        String key = new StringBuilder(account.getSAccount()).append("-")
                .append(account.getERomAccountType()).toString();

        mPhoneNumMap.put(key, phone);
        SharedPreferences sharedPreferences = WalletApp.sGlobalCtx.getSharedPreferences(CACHEFILE,
                0);
        Editor editor = sharedPreferences.edit();
        editor.putString(key, phone);
        editor.commit();
    }
}
