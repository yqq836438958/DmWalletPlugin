package com.pacewear.tws.phoneside.wallet.watch;

import qrom.component.log.QRomLog;
import com.tencent.tws.framework.common.Device;
import com.tencent.tws.framework.common.ICommandHandler;
import com.tencent.tws.framework.common.MsgCmdDefine;
import com.tencent.tws.framework.common.TwsMsg;
import com.tencent.tws.pay.PayNFCConstants;
import com.tencent.tws.proto.wallet.WatchNFCManager.MSG;

public class WatchBaseHandler implements ICommandHandler {
    private static final String TAG = PayNFCConstants.TAG + "."
            + WatchBaseHandler.class.getSimpleName();

    private volatile static WatchBaseHandler g_instance = null;
    private static Object g_instance_lock = new Object();

    public static WatchBaseHandler getInstance() {
        if (g_instance == null) {
            synchronized (g_instance_lock) {
                if (g_instance == null)
                    g_instance = new WatchBaseHandler();
            }
        }

        return g_instance;
    }

    @Override
    public boolean doCommand(TwsMsg oMsg, Device oDeviceFrom) {
        try {
            if (oMsg == null) {
                QRomLog.e(TAG, "doCommand|invalid arg");
                return false;
            }
            QRomLog.d(TAG, "doCommand|cmd=" + oMsg.cmd());

            switch (oMsg.cmd()) {
            case MsgCmdDefine.CMD_NFC_WALLET_MSG_FROM_WATCH:
                MSG msg = new MSG();
                msg.readFrom(oMsg.getInputStreamUTF8());
                WatchHandler.getInstance().dispatchMsgFromWatch(msg);
                break;
            default:
                break;
            }

        } catch (Exception e) {
            QRomLog.e(TAG, "doCommand|exp:" + e.getMessage());
            e.printStackTrace();
        }
        return true;
    }
}
