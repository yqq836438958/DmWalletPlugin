
package com.pacewear.tws.phoneside.wallet.sdkadapter;

import com.pacewear.httpserver.HttpServiceChecker.IServerErrChecker;
import com.tencent.tws.phoneside.business.AccountManager;

public class WalletHttpErrChecker implements IServerErrChecker {
    @Override
    public void onTokenInvalid() {
        AccountManager.getInstance().refreshLoginAccessToken();// 刷新账号的token
    }
}
