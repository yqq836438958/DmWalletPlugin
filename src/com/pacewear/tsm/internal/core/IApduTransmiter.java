
package com.pacewear.tsm.internal.core;

import com.pacewear.tsm.server.tosservice.TSMTosService;

public interface IApduTransmiter {
    public boolean selectAID(String aid, OnTsmProcessCallback callback);

    public boolean transmit(TSMTosService service, OnTsmProcessCallback callback);

    public boolean close();
}
