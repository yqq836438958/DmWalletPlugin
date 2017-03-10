
package com.pacewear.tws.wallet.service;

public class SEResult {
    private int iRet;
    private byte[] bsOutPut;

    public int getInt() {
        return iRet;
    }

    public byte[] getBytes() {
        return bsOutPut;
    }

    public void putInt(int i) {
        iRet = i;
    }

    public void putBytes(byte[] bs) {
        bsOutPut = bs;
    }
}
