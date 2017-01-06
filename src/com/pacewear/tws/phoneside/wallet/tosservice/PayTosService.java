
package com.pacewear.tws.phoneside.wallet.tosservice;

import com.pacewear.httpserver.BaseTosService;
import com.pacewear.tws.phoneside.wallet.env.EnvManager;
import com.qq.taf.jce.JceStruct;

public abstract class PayTosService extends BaseTosService {

    @Override
    protected JceStruct getJceHeader() {
        return EnvManager.getInstanceInner().getPayReqHead();
    }

}
