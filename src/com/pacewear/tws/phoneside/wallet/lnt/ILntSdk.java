
package com.pacewear.tws.phoneside.wallet.lnt;

import android.content.Context;

public interface ILntSdk {
    public boolean charge(Context context, ILntInvokeCallback callback);

    public void complaint(Context context);

    public void complaintQuery(Context context);

    public void clear();
}
