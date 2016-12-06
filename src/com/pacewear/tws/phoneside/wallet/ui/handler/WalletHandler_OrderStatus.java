
package com.pacewear.tws.phoneside.wallet.ui.handler;

import com.pacewear.tws.phoneside.wallet.card.CardManager;
import com.pacewear.tws.phoneside.wallet.card.ICard;
import com.pacewear.tws.phoneside.wallet.order.IOrder;
import com.pacewear.tws.phoneside.wallet.order.IOrderManager.ORDER_STEP;
import com.pacewear.tws.phoneside.wallet.order.OrderManager;
import com.pacewear.tws.phoneside.wallet.step.IStep.STATUS;

import TRom.E_PAY_SCENE;
import TRom.OrderReqParam;

public class WalletHandler_OrderStatus extends WalletBaseHandler {
    private String mTrade = "";

    public WalletHandler_OrderStatus() {
        super();
        mCurModule = MODULE_CALLBACK.MODULE_ORDER;
        mWhiteListScenes = new ACTVITY_SCENE[] {
                ACTVITY_SCENE.SCENE_ISSE_TOPUP
        };
    }

    public void exec(String tradeNo, ORDER_STEP step, STATUS status) {
        // 自己处理 无需交給parent处理
        boolean handle = false;
        switch (status) {
            case HANDLE:
                switch (step) {
                    case ORDER_FINISH:
                        mEventResult = 0;
                        handle = true;
                        break;
                    case ORDER_PAID_FAILED:
                        mEventResult = -1;
                        handle = true;
                        break;
                    default:
                        break;
                }
                break;
            case KEEP:
                mEventResult = -1;
                handle = true;
                break;
            default:
                break;

        }
        if (!handle) {
            return;
        }
        mTrade = tradeNo;
        handleByChild = true;
        super.exec(null, status);
    }

    @Override
    protected int onPostHandle() {
        if (mEventResult != 0) {
            return WALLET_HANDLED;
        }
        IOrder order = OrderManager.getInstance().getOrder(mTrade);
        OrderReqParam reqParam = order.getOrderReqParam();
        ICard card = CardManager.getInstance().getCard(mSceneAID);
        if (reqParam == null || reqParam.getStBusCardBaseInfo() == null
                || card == null) {
            return WALLET_HANDLE_EXCEPTION;
        }
        if (reqParam.getEPayScene() == E_PAY_SCENE._EPS_OPEN_CARD) {
            CardManager.getInstance().forceUpdate(true);
        } else {
            if (reqParam != null && reqParam.getStBusCardBaseInfo() != null) {
                if (card != null) {
                    card.forceUpdate();
                }
            }
        }
        return WALLET_HANDLE_IGNORE;
    }

}
