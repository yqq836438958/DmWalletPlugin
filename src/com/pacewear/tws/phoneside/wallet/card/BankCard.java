package com.pacewear.tws.phoneside.wallet.card;

import TRom.BankCardInfo;

public class BankCard extends Card implements IBankCard{

    @Override
    public String getCardNumber() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected boolean parseCardInfo(String cardInfo) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public BankCardInfo getBankCardInfo() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void clear() {
        // TODO Auto-generated method stub
    }

}
