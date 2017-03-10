
package com.pacewear.tws.wallet.service;

public interface ISeInvoker {
    public int selectAID(String aid);

    public int close();

    public byte[] transmit(byte[] bs);
}
