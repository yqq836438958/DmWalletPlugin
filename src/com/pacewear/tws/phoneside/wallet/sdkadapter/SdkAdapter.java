
package com.pacewear.tws.phoneside.wallet.sdkadapter;

import com.pacewear.httpserver.HttpServiceChecker;
import com.pacewear.tsm.TsmService;
import com.pacewear.tws.phoneside.wallet.WalletApp;

public class SdkAdapter {
    public static void init() {
        HttpServiceChecker.regist(new WalletHttpErrChecker());
        TsmService.getInstance().register(WalletApp.sGlobalCtx, SnowBallCardChannel.get());
        // TODO
    }
}
