
package com.pacewear.tws.phoneside.wallet.tosservice;

import TRom.BusCardInfo;
import TRom.CustomServiceReq;
import TRom.CustomServiceRsp;
import TRom.PayReqHead;

import com.qq.taf.jce.JceStruct;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class CustomService extends TosService {

    private String mName = null;

    private String mPhoneNum = null;

    private ArrayList<BusCardInfo> mCardList = null;

    private static final String TAG = CustomService.class.getSimpleName();

    @Override
    public String getFunctionName() {
        return "customService";
    }

    public void setUserPersonalInfo(final String name, final String phoneNum, ArrayList<BusCardInfo> cardList) {
        mName = name;
        mPhoneNum = phoneNum;
        mCardList = cardList;
    }

    @Override
    public JceStruct getReq(PayReqHead payReqHead) {

        return new CustomServiceReq(payReqHead.getStDeviceBaseInfo(), payReqHead.stUserAuthInfo,
                payReqHead.getStSEBaseInfo(),
                mName, mPhoneNum, mCardList, generateTimestamp());
    }

    private String generateTimestamp() {
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS");
        return sdf.format(date);

    }

    @Override
    public JceStruct getRspObject() {
        return new CustomServiceRsp();
    }

    public int getOperType() {
        return OPERTYPE_CUSTOMERSUPPORT;
    }

}
