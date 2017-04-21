
package com.pacewear.tws.phoneside.wallet.lnt;

import android.text.TextUtils;

import com.pacewear.common.utils.CacheUtils;
import com.pacewear.tws.phoneside.wallet.lnt.vip.CheckVerifyCode;
import com.pacewear.tws.phoneside.wallet.lnt.vip.GetLntPhoneNumber;
import com.pacewear.tws.phoneside.wallet.lnt.vip.SyncUserInfo;
import com.tencent.tws.phoneside.phoneverify.SmsFuncFactory;
import com.tencent.tws.phoneside.phoneverify.SmsModel;
import com.tencent.tws.phoneside.phoneverify.SmsModel.OnSmsCallback;

public class LntVip implements ILntVip {
    private static final String LNT_CACHE_FILE = "lnt_cache_file";
    private static final String LNT_VIP_KEY = "lnt_vip_account";
    private ILntVipCallback mLntVipCallback = null;

    public LntVip(ILntVipCallback callback) {
        mLntVipCallback = callback;
    }

    private void saveVipPhone(String phone) {
        CacheUtils.prepare(LNT_CACHE_FILE);
        CacheUtils.save(LNT_VIP_KEY, phone);
    }

    private String getLocalCacheVipPhone() {
        CacheUtils.prepare(LNT_CACHE_FILE);
        String phone = CacheUtils.get(LNT_VIP_KEY, "");
        return phone;
    }

    private void initLntSms() {
        SmsFuncFactory.get().regist(SmsFuncFactory.SERVICE_GET_PHONE, GetLntPhoneNumber.class);
        SmsFuncFactory.get().regist(SmsFuncFactory.SERVICE_GET_VERIFYCODE, SyncUserInfo.class);
        SmsFuncFactory.get().regist(SmsFuncFactory.SERVICE_SEND_VERIFYCODE, CheckVerifyCode.class);
    }

    private void deinitLntSms() {
        SmsFuncFactory.get().clear();
    }

    private void getRemoteVipPhone() {
        initLntSms();
        OnSmsCallback callback = new OnSmsCallback() {

            @Override
            public void OnResult(int ret, String result) {
                if (ret == 0 && !TextUtils.isEmpty(result)) {
                    saveVipPhone(result);
                    if (mLntVipCallback != null) {
                        mLntVipCallback.onSuc(result);
                    }
                } else {
                    if (mLntVipCallback != null) {
                        mLntVipCallback.onTransfer(SmsModel.get().getVerifyPage());
                    }
                }
            }
        };
        SmsModel.get().getPhoneNum(callback);
    }

    @Override
    public String getId() {
        String localPhone = getLocalCacheVipPhone();
        if (TextUtils.isEmpty(localPhone)) {
            getRemoteVipPhone();
        }
        return localPhone;
    }

    @Override
    public void saveId(String ursId) {
        saveVipPhone(ursId);
    }

    @Override
    public void destroy() {
        deinitLntSms();
    }
}
