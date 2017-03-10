
package com.pacewear.tws.phoneside.wallet.lnt;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.pacewear.common.utils.CacheUtils;
import com.pacewear.httpserver.ServerHandler;
import com.pacewear.tws.phoneside.wallet.R;
import com.pacewear.tws.phoneside.wallet.card.ICardInner.CONFIG;
import com.pacewear.tws.phoneside.wallet.common.DeviceUtil;
import com.pacewear.tws.phoneside.wallet.common.PackageUtils;
import com.pacewear.tws.phoneside.wallet.lnt.vip.CheckVerifyCode;
import com.pacewear.tws.phoneside.wallet.lnt.vip.GetLntPhoneNumber;
import com.pacewear.tws.phoneside.wallet.lnt.vip.SyncUserInfo;
import com.tencent.tws.phoneside.phoneverify.SmsFuncFactory;
import com.tencent.tws.phoneside.phoneverify.SmsModel;
import com.tencent.tws.phoneside.phoneverify.SmsModel.OnSmsCallback;

public class LntChargeSdk implements ILntChargeSdk {
    public static interface ILntCardPage {
        public boolean jump(String className);
    }

    private static final String LNT_CACHE_FILE = "lnt_cache_file";
    private static final String LNT_VIP_KEY = "lnt_vip_account";
    private ILntCardPage mCardPage = null;
    private ILntInvokeCallback mILntInvokeCallback = null;
    private Context mContext = null;

    public LntChargeSdk(Context context, ILntCardPage cardPage, ILntInvokeCallback callback) {
        mContext = context;
        mCardPage = cardPage;
        mILntInvokeCallback = callback;
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
                    invokeChargeSdk(result);
                } else {
                    if (mCardPage != null) {
                        mCardPage.jump(SmsModel.get().getVerifyPage());
                    }
                }
            }
        };
        SmsModel.get().getPhoneNum(callback);
    }

    @Override
    public boolean invoke(String aid, String phone) {
        if (!CONFIG.LINGNANTONG.mAID.equals(aid)) {
            return false;
        }
        String localPhone = !TextUtils.isEmpty(phone) ? phone : getLocalCacheVipPhone();
        if (!TextUtils.isEmpty(localPhone)) {
            invokeChargeSdk(localPhone);
            saveVipPhone(localPhone);
            return true;
        }
        getRemoteVipPhone();
        return true;
    }

    private void invokeChargeSdk(final String usrId) {
        // 运行在主线程
        Handler mainHandler = new Handler(Looper.getMainLooper());
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                invokeChargeSdkInternal(usrId);
            }
        });

    }

    private void invokeChargeSdkInternal(String usrId) {
        String macAddr = DeviceUtil.getConnectedDeviceAddr();
        LntSdkContext sdkContext = LntSdkContextWrapper.newInstance(mContext,
                PackageUtils.getHostAppName(), macAddr,
                mContext.getString(R.string.wallet_default_cardcity), usrId,
                !ServerHandler.getInstance(mContext).isTestEnv(), mILntInvokeCallback);
        LntLauncher.cardTopup(sdkContext);
    }

    @Override
    public void destroy() {
        LntLauncher.clear();
        deinitLntSms();
    }
}
