
package com.pacewear.tws.phoneside.wallet.transaction;

import java.util.ArrayList;

public interface ITransactionCallback {
    public void onRsp(ArrayList<CardTransactItem> list);
}
