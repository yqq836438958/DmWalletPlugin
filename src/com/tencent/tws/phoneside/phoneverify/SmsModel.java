
package com.tencent.tws.phoneside.phoneverify;

import android.text.TextUtils;

import com.pacewear.common.utils.CacheUtils;
import com.pacewear.httpserver.IResponseObserver;
import com.qq.taf.jce.JceStruct;
import com.tencent.tws.phoneside.business.AccountManager;
import com.tencent.tws.phoneside.phoneverify.ISmsDataHandler.ParseResult;

import java.util.HashMap;

import TRom.RomAccountInfo;

public class SmsModel {
    public static final String TAG = "SmsModel";
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

    public static SmsModel getCustom(int platform) {
        if (sInstance == null) {
            synchronized (SmsModel.class) {
                sInstance = new SmsModel();
            }
        }
        return sInstance;
    }
    public void getSecurityCode(String phone, OnSmsCallback callback) {
        SmsTosService getSecurityCode = SmsFuncFactory.get().getSecurityCode(phone, null);
        sendTosServiceReq(phone, getSecurityCode, callback);
    }

    public void sendSecurityCode(String phone, String verifyCode, OnSmsCallback callback) {
        SmsTosService sendSecurityCode = SmsFuncFactory.get().sendVerifyCode(phone, verifyCode);
        sendTosServiceReq(phone, sendSecurityCode, callback);
    }

    public void getPhoneNum(final OnSmsCallback callback) {
        final SmsTosService getPhoneNumber = SmsFuncFactory.get().getPhoneNum();
        final long lSeq = getPhoneNumber.getUniqueSeq();
        getPhoneNumber.invoke(new IResponseObserver() {

            @Override
            public void onResponseSucceed(long uniqueSeq, int operType, JceStruct response) {
                if (lSeq == uniqueSeq) {
                    ParseResult parseResult = ((ISmsDataHandler) getPhoneNumber).onParse(response);
                    postResult(callback, parseResult.iRet, parseResult.msg);
                    ((ISmsDataHandler) getPhoneNumber).onPostHandle(parseResult.msg);
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
                    ParseResult parseResult = ((ISmsDataHandler) service).onParse(response);
                    postResult(callback, parseResult.iRet, parseResult.msg);
                    ((ISmsDataHandler) service).onPostHandle(phone);
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
        CacheUtils.prepare(CACHEFILE);
        String phone = CacheUtils.get(key, "");
        if (TextUtils.isEmpty(phone)) {
            return phone;
        }
        mPhoneNumMap.put(key, phone);
        return phone;
    }

    public void saveGlobalPhoneNum(String phone) {
        RomAccountInfo account = AccountManager.getInstance().getLoginAccountIdInfo();
        if (account == null || TextUtils.isEmpty(account.getSAccount())) {
            return;
        }
        String key = new StringBuilder(account.getSAccount()).append("-")
                .append(account.getERomAccountType()).toString();
        CacheUtils.prepare(CACHEFILE);
        CacheUtils.save(key, phone);
        mPhoneNumMap.put(key, phone);
    }

    public String getVerifyPage() {
        return PhoneVerifyActivity.class.getName();
    }
}
