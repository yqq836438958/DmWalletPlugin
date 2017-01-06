
package com.pacewear.tsm;

public interface ITsmBusinessListener {
    public void onSuccess(String desc);

    public void onFail(int error, String desc);
}
