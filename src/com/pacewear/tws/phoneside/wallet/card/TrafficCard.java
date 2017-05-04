
package com.pacewear.tws.phoneside.wallet.card;

import TRom.BusCardBaseInfo;
import TRom.PayConfig;
import android.text.TextUtils;

import com.pacewear.tws.phoneside.wallet.order.IOrderManagerInner;
import com.pacewear.tws.phoneside.wallet.order.OrderManager;

import org.json.JSONException;
import org.json.JSONObject;

import qrom.component.log.QRomLog;

/**
 * @author baodingzhou
 */

public class TrafficCard extends Card implements ITrafficCard {

    // 初始值未后台定义
    private String mCardNumber = "00000000";

    private String mBalance = null;

    private String mValidity = null;

    private String mStartDate = "";

    @Override
    protected String getTAG() {
        return "TrafficCard";
    }

    @Override
    public String getCardNumber() {
        return mCardNumber;
    }

    @Override
    public String getBalance() {
        if (TextUtils.isEmpty(mBalance)) {
            return "";
        }
        return mBalance;
    }

    @Override
    protected boolean parseCardInfo(String cardInfo) {
        QRomLog.d(getTAG(), "parseCardInfo " + cardInfo);

        boolean handled = false;

        JSONObject root = null;

        try {
            root = new JSONObject(cardInfo);
        } catch (JSONException e) {
            QRomLog.e(getTAG(), e.getMessage());
            return handled;
        }

        if (root.has("balance")) {
            try {
                mBalance = root.getString("balance");
                handled = true;
            } catch (JSONException e) {
                QRomLog.e(getTAG(), e.getMessage());
                handled = false;
            }
        }

        if (root.has("card_number")) {
            try {
                mCardNumber = root.getString("card_number");
            } catch (JSONException e) {
                QRomLog.e(getTAG(), e.getMessage());
                handled = false;
            }
        }

        if (root.has("validity")) {
            try {
                mValidity = root.getString("validity");
            } catch (JSONException e) {
                QRomLog.e(getTAG(), e.getMessage());
                handled = false;
            }
        }

        if (root.has("startdate")) {
            try {
                mStartDate = root.getString("startdate");
            } catch (JSONException e) {
                QRomLog.e(getTAG(), e.getMessage());
                handled = false;
            }
        }
        if (handled) {

        }
        return handled;
    }

    @Override
    public BusCardBaseInfo getBusCardBaseInfo() {

        // TODO
        IOrderManagerInner orderManager = OrderManager.getInstanceInner();
        PayConfig payconfig = orderManager.getPayConfig(getAID());
        if (payconfig == null) {
            return null;
        }

        BusCardBaseInfo busCardBaseInfo = new BusCardBaseInfo(getCardNumber(),
                payconfig.sIssuerName, getAID());
        return busCardBaseInfo;
    }

    @Override
    public String getValidity() {
        return mValidity;
    }

    @Override
    public void clear() {
        mValidity = "";
        mStartDate = "";
        mBalance = "";
        mCardNumber = "";
        mCardInfoErrDesc = null;
        setActivationStatus(ACTIVATION_STATUS.UNACTIVATED);
        setInstallStatus(INSTALL_STATUS.UNINSTALLED);
    }

    @Override
    public String getStartDate() {
        return mStartDate;
    }
}
