
package com.pacewear.tws.phoneside.wallet.tosservice;

import com.pacewear.httpserver.BaseTosService;

import TRom.IssuerInfo;

/**
 * @author baodingzhou
 */

public abstract class AbstractBankReq extends PayTosService {

    protected IssuerInfo mIssuerInfo = null;

    protected String mCardPan = null;

    public final AbstractBankReq setIssuerInfo(IssuerInfo issuerInfo) {
        mIssuerInfo = issuerInfo;
        return this;
    }

    public final AbstractBankReq setCardPan(String cardPan) {
        mCardPan = cardPan;
        return this;
    }
}
