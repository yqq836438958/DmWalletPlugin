
package com.pacewear.tws.wallet.service;

import android.util.Log;

import com.pacewear.tws.phoneside.wallet.common.ByteUtil;
import com.pacewear.tws.phoneside.wallet.walletservice.ApduExchange;
import com.pacewear.tws.phoneside.wallet.walletservice.IResult;
import com.pacewear.tws.phoneside.wallet.walletservice.SelectAid;
import com.pacewear.tws.phoneside.wallet.walletservice.ShutDown;
import com.pacewear.tws.phoneside.wallet.walletservice.WalletService;

import java.util.concurrent.Semaphore;

public class WalletSeInvoker implements ISeInvoker {
    public static final String TAG = "Se";

    @Override
    public int selectAID(String aid) {
        SelectAid walletService = new SelectAid();
        walletService.putString(aid);
        SEResult invokeRet = invoke(walletService);
        return invokeRet.getInt();
    }

    @Override
    public int close() {
        WalletService shutdown = new ShutDown();
        SEResult invokeRet = invoke(shutdown);
        return invokeRet.getInt();
    }

    @Override
    public byte[] transmit(byte[] apdus) {
        WalletService walletService = null;
        byte[] aid = tryGetAIDIfNeed(apdus);
        boolean isSelectApdu = (aid != null);
        if (isSelectApdu) {
            walletService = new SelectAid();
            walletService.putString(ByteUtil.toHexString(aid));
        } else {
            walletService = new ApduExchange();
            walletService.putBytes(apdus);
        }
        SEResult invokeRet = invoke(walletService);
        if (invokeRet.getBytes() != null) {
            Log.d(TAG, ByteUtil.toHexString(invokeRet.getBytes()));
        }
        if (invokeRet.getInt() == 0 && invokeRet.getBytes() == null) {
            invokeRet.putBytes(ByteUtil.toByteArray("9000"));
        }
        return invokeRet.getBytes();
    }

    private SEResult invoke(WalletService service) {
        Log.d(TAG, "invoke Thread:" + Thread.currentThread().getName());
        final SEResult invokeResult = new SEResult();
        final Semaphore semaphore = new Semaphore(0);
        final long lReq = service.getSeqID();
        boolean handle = service.invoke(new IResult() {

            @Override
            public void onResult(long seqID, int ret, String[] outputParams, Integer[] resultCode,
                    byte[] bytes) {
                if (lReq == seqID) {
                    Log.d(TAG, "callback onResult Thread:" + Thread.currentThread().getName());
                    invokeResult.putInt(ret);
                    invokeResult.putBytes(bytes);
                    semaphore.release();
                }
            }

            @Override
            public void onExecption(long seqID, int error) {
                if (lReq == seqID) {
                    Log.d(TAG, "callback onExecption Thread:" + Thread.currentThread().getName());
                    invokeResult.putInt(error);
                    invokeResult.putBytes(null);
                    semaphore.release();
                }
            }
        });
        if (handle) {
            try {
                semaphore.acquire();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        Log.d(TAG, "finally...Thread:" + Thread.currentThread().getName());
        return invokeResult;
    }

    private byte[] tryGetAIDIfNeed(byte[] apdu) {
        byte[] result = null;
        if (apdu.length < 5) {
            return result;
        }
        if (apdu[0] == 0x00 && apdu[1] == (byte) 0xA4 && apdu[2] == 0x04) {
            int aidLen = apdu[4];
            if (aidLen != 0) {
                result = new byte[aidLen];
                System.arraycopy(apdu, 5, result, 0, aidLen);
            }
        }
        return result;
    }
}
