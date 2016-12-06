
package com.pacewear.tws.phoneside.wallet.ui.handler;

import com.pacewear.tws.phoneside.wallet.card.CardManager;
import com.pacewear.tws.phoneside.wallet.card.ICardManagerListener;
import com.pacewear.tws.phoneside.wallet.order.IOrderManager.ORDER_STEP;
import com.pacewear.tws.phoneside.wallet.order.IOrderManagerListener;
import com.pacewear.tws.phoneside.wallet.order.OrderManager;
import com.pacewear.tws.phoneside.wallet.step.IStep.COMMON_STEP;
import com.pacewear.tws.phoneside.wallet.step.IStep.STATUS;
import com.pacewear.tws.phoneside.wallet.ui.handler.WalletBaseHandler.ACTVITY_SCENE;
import com.pacewear.tws.phoneside.wallet.ui.handler.WalletBaseHandler.OnWalletUICallback;

public class WalletHandlerManager implements ICardManagerListener, IOrderManagerListener {
    private static WalletHandlerManager sInstance = null;
    private WalletBaseHandler[] mHandles = new WalletBaseHandler[5];

    public static WalletHandlerManager getInstance() {
        if (sInstance == null) {
            synchronized (WalletHandlerManager.class) {
                if (sInstance == null) {
                    sInstance = new WalletHandlerManager();
                    sInstance.init();
                }
            }
        }
        return sInstance;
    }

    private void init() {
        mHandles[0] = new WalletHandler_NewOrder();
        mHandles[1] = new WalletHandler_OrderStatus();
        mHandles[2] = new WalletHandler_OrderManager();
        mHandles[3] = new WalletHandler_CardList();
        mHandles[4] = new WalletHandler_CardInfo();
        CardManager.getInstance().registerCardManagerListener(this);
        OrderManager.getInstance().registerOrderManagerListener(this);
    }

    public void unInit() {
        CardManager.getInstance().unregisterCardManagerListener(this);
        OrderManager.getInstance().unregisterOrderManagerListener(this);
    }

    public void register(String aid, ACTVITY_SCENE scene, OnWalletUICallback callback) {
        for (WalletBaseHandler handler : mHandles) {
            handler.registCallback(aid, scene, callback);
        }
    }

    public void unregister(ACTVITY_SCENE scene) {
        for (WalletBaseHandler handler : mHandles) {
            handler.unregistCallback(scene);
        }
    }

    @Override
    public void onNewOrder(long uniqueSeq, boolean succeed, String tradeNo) {
        ((WalletHandler_NewOrder) mHandles[0]).exec(uniqueSeq, succeed, tradeNo);
    }

    @Override
    public void onOrderStatus(String tradeNo, ORDER_STEP step, STATUS status) {
        ((WalletHandler_OrderStatus) mHandles[1]).exec(tradeNo, step, status);
    }

    @Override
    public void onOrderManager(MODULE module, COMMON_STEP step, STATUS status) {
        ((WalletHandler_OrderManager) mHandles[2]).exec(module, step, status);
    }

    @Override
    public void onCardListMsg(COMMON_STEP step, STATUS status) {
        mHandles[3].exec(step, status);
    }

    @Override
    public void onCardMsg(String aid, COMMON_STEP step, STATUS status) {
        ((WalletHandler_CardInfo) mHandles[4]).exec(aid, step, status);
    }

    public void requestFocus(ACTVITY_SCENE scene) {
        WalletBaseHandler.setCurScene(scene);
    }
}
