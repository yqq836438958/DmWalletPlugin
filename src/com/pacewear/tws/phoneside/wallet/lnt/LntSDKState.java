
package com.pacewear.tws.phoneside.wallet.lnt;

public class LntSDKState {
    private int mState = -1;
    private ILntSdk mSdk = null;

    public LntSDKState(ILntSdk sdk) {
        mSdk = sdk;
    }

    public void resume() {
        switch (mState) {
            case 0:
                mSdk.charge();
                break;
            case 1:
                mSdk.complaint();
                break;
            case 2:
                mSdk.complaintQuery();
                break;
            default:
                break;
        }
    }

    public void pause(int state) {
        mState = state;
    }

    public void clear() {
        mState = -1;
    }
}
