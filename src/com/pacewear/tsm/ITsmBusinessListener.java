
package com.pacewear.tsm;

public interface ITsmBusinessListener {
    public void onSuccess(/*int reqId, */String desc);

    public void onFail(/*int reqId,*/ int error, String desc);
}
