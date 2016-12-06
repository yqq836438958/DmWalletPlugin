
package com.pacewear.tws.phoneside.wallet.transaction;

import com.pacewear.tws.phoneside.wallet.walletservice.IResult;
import com.pacewear.tws.phoneside.wallet.walletservice.TransQuerySe;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class CardTransaction {
    public boolean query(String aid, final ITransactionCallback callback) {
        TransQuerySe transQuerySe = new TransQuerySe();
        final long uniqueReq = transQuerySe.getSeqID();
        transQuerySe.putString(aid);
        boolean handled = transQuerySe.invoke(new IResult() {

            @Override
            public void onResult(long seqID, int ret, String[] outputParams, Integer[] resultCode,
                    byte[] bytes) {
                if (seqID == uniqueReq) {
                    String info = (outputParams != null && outputParams.length > 0)
                            ? outputParams[0] : null;
                    ArrayList<CardTransactItem> targetList = null;
                    if (ret == 0 && info != null) {
                        targetList = parseTransactLists(info);
                    }
                    if (callback != null) {
                        callback.onRsp(targetList);
                    }
                }
            }

            @Override
            public void onExecption(long seqID, int error) {
                if (callback != null) {
                    callback.onRsp(null);
                }
            }
        });
        return handled;
    }

    private ArrayList<CardTransactItem> parseTransactLists(String strRet) {
        ArrayList<CardTransactItem> targetList = null;
        JSONObject root = null;
        JSONObject tmp = null;
        try {
            root = new JSONObject(strRet);
        } catch (Exception e) {
            return null;
        }
        JSONArray array = root.optJSONArray("records");
        if (array == null || array.length() <= 0) {
            return null;
        }
        for (int i = 0; i < array.length(); i++) {
            tmp = array.optJSONObject(i);
            if (tmp != null) {
                if (targetList == null) {
                    targetList = new ArrayList<CardTransactItem>();
                }
                CardTransactItem item = new CardTransactItem();
                item.iType = tmp.optInt("transaction_type");
                item.iStatus = tmp.optInt("transaction_status");
                item.strTime = tmp.optString("transaction_time");
                item.lAmount = tmp.optLong("transaction_amount");
                targetList.add(item);
            }
        }
        return targetList;
    }
}
