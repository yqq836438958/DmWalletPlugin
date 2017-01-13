
package com.pacewear.tsm.internal.core;

public interface OnTsmProcessCallback {
    public void onSuccess(String[] apduList);

    public void onFail(int ret, String desc);
}
