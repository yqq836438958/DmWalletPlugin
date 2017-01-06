
package com.pacewear.tsm.channel;

public interface ITsmAPDUCallback {
    public void onSuccess(String[] apdus);

    public void onFail(int error, String desc);
}
