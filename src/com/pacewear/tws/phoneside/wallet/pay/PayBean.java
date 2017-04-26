
package com.pacewear.tws.phoneside.wallet.pay;

import com.pacewear.tws.phoneside.wallet.R;

public enum PayBean {


    WEIXIN_PAY(IPayManagerInner.PAY_WEIXIN, R.drawable.ic_wechatpay, R.string.wallet_pay_channel_wechat), 
    QQ_PAY(IPayManagerInner.PAY_QQ, R.drawable.ic_qqwallet, R.string.wallet_pay_channel_qq);

    private int iPayType;
    private int iPayIcon;
    private int sPayName;

    private PayBean(
            int type,
            int icRes, int nameRes) {
        iPayType = type;
        iPayIcon = icRes;
        sPayName = nameRes;
    }

    public int getType() {
        return iPayType;
    }

    public int getIcon() {
        return iPayIcon;
    }

    public int getName() {
        return sPayName;
    }
}
