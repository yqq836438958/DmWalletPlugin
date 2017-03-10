
package com.pacewear.tws.phoneside.wallet.lnt;

public interface ILntChargeSdk {
    public boolean invoke(String aid, String phone);

    public void destroy();
}
