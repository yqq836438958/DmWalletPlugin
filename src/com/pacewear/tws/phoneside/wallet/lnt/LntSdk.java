
package com.pacewear.tws.phoneside.wallet.lnt;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.pacewear.httpserver.ServerHandler;
import com.pacewear.tws.phoneside.wallet.R;
import com.pacewear.tws.phoneside.wallet.common.DeviceUtil;
import com.pacewear.tws.phoneside.wallet.common.PackageUtils;
import com.pacewear.tws.phoneside.wallet.lnt.ILntVip.ILntVipCallback;

public class LntSdk implements ILntSdk {

    @Override
    public boolean resume(String content) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean charge() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void complaint() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void complaintQuery() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void destroy() {
        // TODO Auto-generated method stub
        
    }/*
    private ILntCardPage mCardPage = null;
    private ILntInvokeCallback mILntInvokeCallback = null;
    private Context mContext = null;
    private ILntVip mLntVip = null;
    private LntSDKState mLntSDKState = null;
    private Handler mThreadHandler = null;

    public LntSdk(Context context, ILntCardPage cardPage,
            ILntInvokeCallback callback) {
        mContext = context;
        mCardPage = cardPage;
        mILntInvokeCallback = callback;
        mLntSDKState = new LntSDKState(this);
        mThreadHandler = new Handler(Looper.getMainLooper());
        initVip();
    }

    private void initVip() {
        ILntVipCallback callback = new ILntVipCallback() {

            @Override
            public void onTransfer(String className) {
                if (mCardPage != null) {
                    mCardPage.jump(className);
                }
            }

            @Override
            public void onSuc(String usrId) {
                mLntVip.saveId(usrId);
                mLntSDKState.resume();
            }
        };
        mLntVip = new LntVip(callback);
    }

    @Override
    public void destroy() {
        LntLauncher.clear();
        mLntVip.destroy();
    }

    @Override
    public boolean charge() {
        mLntSDKState.clear();
        final String ursId = mLntVip.getId();
        if (TextUtils.isEmpty(ursId)) {
            mLntSDKState.pause(0);
            return true;
        }
        mThreadHandler.post(new Runnable() {

            @Override
            public void run() {
                LntLauncher.cardTopup(newLntSdkCtx(ursId));
            }
        });
        return true;
    }

    @Override
    public void complaint() {
        mLntSDKState.clear();
        final String ursId = mLntVip.getId();
        if (TextUtils.isEmpty(ursId)) {
            mLntSDKState.pause(1);
            return;
        }
        mThreadHandler.post(new Runnable() {

            @Override
            public void run() {
                LntLauncher.complaint(newLntSdkCtx(ursId));
            }
        });

    }

    @Override
    public void complaintQuery() {
        mLntSDKState.clear();
        final String ursId = mLntVip.getId();
        if (TextUtils.isEmpty(ursId)) {
            mLntSDKState.pause(2);
            return;
        }
        mThreadHandler.post(new Runnable() {

            @Override
            public void run() {
                LntLauncher.complaintQuery(newLntSdkCtx(ursId));
            }
        });

    }

    private LntSdkContext newLntSdkCtx(String usrId) {
        String macAddr = DeviceUtil.getConnectedDeviceAddr();
        return LntSdkContextWrapper.newInstance(mContext,
                PackageUtils.getHostAppName(), macAddr,
                mContext.getString(R.string.wallet_default_cardcity), usrId,
                !ServerHandler.getInstance(mContext).isTestEnv(), mILntInvokeCallback);
    }

    @Override
    public boolean resume(String content) {
        if (TextUtils.isDigitsOnly(content)) {
            mLntVip.saveId(content);
        }
        mLntSDKState.resume();
        return true;
    }

*/}
