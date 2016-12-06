
package com.pacewear.tws.phoneside.wallet.rpc;

import com.pacewear.tws.phoneside.wallet.common.SeqGenerator;
import com.tencent.tws.proto.wallet.WatchNFCManager.MSG;
import com.tencent.tws.proto.wallet.WatchNFCManager.MSG_RPC;
import com.tencent.tws.proto.wallet.WatchNFCManager.MSG_RPC_METHOD;
import com.tencent.tws.proto.wallet.WatchNFCManager.MSG_RPC_PARAMS;

import java.util.ArrayList;

/**
 * @author baodingzhou
 */

public abstract class RPCMethod<Iterface> implements IRPC<Iterface>, IRPCHandlerListener {

    protected Iterface mListener = null;

    private IRPCHandler mRPCHandler = null;

    long mSeq = -1;

    private long mTimeoutMillis = 0;

    private int mMethod = MSG_RPC_METHOD._UNKNOWN;

    private MSG_RPC_PARAMS mParams = null;

    public RPCMethod() {
        mSeq = SeqGenerator.getInstance().uniqueSeq();
    }

    @Override
    public long getSeqID() {
        return mSeq;
    }

    @Override
    public boolean invoke(Iterface listener) {

        boolean handled = false;

        if (listener == null) {
            return handled;
        }

        if (mMethod == MSG_RPC_METHOD._UNKNOWN) {
            return handled;
        }

        mListener = listener;

        mRPCHandler = RPCHandler.getInstance();
        mRPCHandler.registerIRPCHandlerListener(this);

        MSG_RPC rpc = new MSG_RPC();
        rpc.eMethod = mMethod;
        if (mParams != null) {
            rpc.stInParams = mParams;
        }

        handled = mRPCHandler.sendRPCMsg(mSeq, rpc, mTimeoutMillis);
        if (!handled) {
            mRPCHandler.unregisterIRPCHandlerListener(this);
        }

        return handled;
    }

    /**
     * onResultReal
     * 
     * @param seq
     * @param result
     */
    public abstract void onResultReal(MSG_RPC_PARAMS result);

    /**
     * onExecptionReal
     * 
     * @param seq
     * @param msg
     */
    public abstract void onExecptionReal(MSG msg);

    @Override
    public final boolean onResult(long seq, MSG_RPC_PARAMS result) {
        if (mSeq != seq) {
            return false;
        }
        onResultReal(result);
        return true;
    }

    @Override
    public final boolean onExecption(long seq, MSG msg) {
        if (mSeq != seq) {
            return false;
        }
        onExecptionReal(msg);
        return true;
    }

    private MSG_RPC_PARAMS getParams() {
        if (mParams == null) {
            mParams = new MSG_RPC_PARAMS();
        }

        return mParams;
    }

    @Override
    public RPCMethod<Iterface> putInt(int i) {
        getParams().iInt = i;
        return this;
    }

    @Override
    public RPCMethod<Iterface> putString(String str) {
        getParams().sStr = str;
        return this;
    }

    @Override
    public RPCMethod<Iterface> putBytes(byte[] bytes) {
        getParams().vBytes = bytes;
        return this;
    }

    @Override
    public RPCMethod<Iterface> putStringArray(String[] strs) {
        if (strs != null && strs.length > 0) {
            getParams().vString = new ArrayList<String>();
            for (int i = 0; i < strs.length; i++) {
                getParams().vString.add(strs[i]);
            }
        }
        return this;
    }

    @Override
    public RPCMethod<Iterface> setTimeoutMillis(long millis) {
        mTimeoutMillis = millis;
        return this;
    }

    @Override
    public RPCMethod<Iterface> method(int method) {
        mMethod = method;
        return this;
    }
}
