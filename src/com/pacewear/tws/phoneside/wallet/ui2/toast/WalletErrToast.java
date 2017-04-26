
package com.pacewear.tws.phoneside.wallet.ui2.toast;

import android.content.Context;

import com.pacewear.tws.phoneside.wallet.R;
import com.pacewear.tws.phoneside.wallet.WalletApp;
import com.pacewear.tws.phoneside.wallet.errcheck.ErrCheck;
import com.pacewear.tws.phoneside.wallet.errcheck.ErrCheck.CardManagerNotReady;
import com.pacewear.tws.phoneside.wallet.errcheck.ErrCheck.OrderManagerNotReady;
import com.tencent.tws.assistant.widget.Toast;
import com.pacewear.tws.phoneside.wallet.errcheck.ErrCheck.DeviceNotConnect;

public class WalletErrToast {
    public static boolean show() {
        return false;
    }

    public static boolean checkAll(final Context context) {
        ErrCheck check = new ErrCheck();
        check.addCheck(new CardManagerNotReady() {

            @Override
            protected void onHandle() {
                Toast.makeText(WalletApp.getHostAppContext(),
                        context.getText(R.string.wallet_sync_err_watch),
                        android.widget.Toast.LENGTH_LONG).show();
            }
        });
        check.addCheck(new OrderManagerNotReady() {

            @Override
            protected void onHandle() {
                Toast.makeText(WalletApp.getHostAppContext(),
                        context.getText(R.string.wallet_sync_err_network),
                        android.widget.Toast.LENGTH_LONG).show();
            }
        });
        check.addCheck(new DeviceNotConnect() {

            @Override
            protected void onHandle() {
                Toast.makeText(WalletApp.getHostAppContext(),
                        context.getString(R.string.wallet_sync_err_watch),
                        Toast.LENGTH_LONG).show();
            }
        });
        return check.invoke();
    }

    public static boolean checkDev() {
        return false;
    }

    public static boolean checkNet() {
        return false;
    }
}
