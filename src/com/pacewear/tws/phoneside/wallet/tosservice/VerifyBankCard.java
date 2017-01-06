
package com.pacewear.tws.phoneside.wallet.tosservice;

import TRom.IssuerInfo;
import TRom.PayBankVerifyCardReq;
import TRom.PayBankVerifyCardRsp;
import TRom.PayReqHead;

import com.qq.taf.jce.JceStruct;

/**
 * @author baodingzhou
 */

public class VerifyBankCard extends PayTosService {

    private IssuerInfo mIssuerInfo = null;

    private String mCardPan = null;

    @Override
    public int getOperType() {
        return OPERTYPE_VERIFY_BANK_CARD;
    }

    @Override
    public String getFunctionName() {
        return "payBankVerifyCard";
    }

    public VerifyBankCard setIssuerInfo(IssuerInfo issuerInfo) {
        mIssuerInfo = issuerInfo;
        return this;
    }

    public VerifyBankCard setCardPan(String cardPan) {
        mCardPan = cardPan;
        return this;
    }

    @Override
    public JceStruct getReq(JceStruct _payReqHead) {
        PayReqHead payReqHead = (PayReqHead) _payReqHead;
        PayBankVerifyCardReq req = new PayBankVerifyCardReq();

        req.stPayReqHead = payReqHead;
        req.stIssuerInfo = mIssuerInfo;
        req.sCardPan = mCardPan;

        return req;
    }

    @Override
    public JceStruct getRspObject() {
        return new PayBankVerifyCardRsp();
    }

}
