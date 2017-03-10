
package com.pacewear.tws.phoneside.wallet.order;

import TRom.BusCardBaseInfo;
import TRom.BusCardStatusInfo;
import TRom.E_PAY_SCENE;
import TRom.GetCardStatusRsp;
import TRom.OrderReqParam;
import TRom.PayBusCardConfigRsp;
import TRom.PayConfig;
import TRom.PayRechargeAmount;

import android.text.TextUtils;

import com.pacewear.httpserver.IResponseObserver;
import com.pacewear.tws.phoneside.wallet.WalletApp;
import com.pacewear.tws.phoneside.wallet.card.CardManager;
import com.pacewear.tws.phoneside.wallet.card.ICardManager;
import com.pacewear.tws.phoneside.wallet.card.ITrafficCard;
import com.pacewear.tws.phoneside.wallet.common.Constants;
import com.pacewear.tws.phoneside.wallet.common.SeqGenerator;
import com.pacewear.tws.phoneside.wallet.common.Utils;
import com.pacewear.tws.phoneside.wallet.common.WalletRunEnv;
import com.pacewear.tws.phoneside.wallet.env.EnvManager;
import com.pacewear.tws.phoneside.wallet.env.IEnvManager;
import com.pacewear.tws.phoneside.wallet.env.IEnvManagerListener;
import com.pacewear.tws.phoneside.wallet.order.IOrderManagerListener.MODULE;
import com.pacewear.tws.phoneside.wallet.step.IStep;
import com.pacewear.tws.phoneside.wallet.step.IStep.COMMON_STEP;
import com.pacewear.tws.phoneside.wallet.step.IStep.STATUS;
import com.pacewear.tws.phoneside.wallet.step.Step;
import com.pacewear.tws.phoneside.wallet.tosservice.BusCardConfig;
import com.pacewear.tws.phoneside.wallet.tosservice.LatestCardStatus;
import com.pacewear.tws.phoneside.wallet.walletservice.IResult;
import com.pacewear.tws.phoneside.wallet.walletservice.PassData;
import com.qq.taf.jce.JceStruct;

import org.json.JSONException;
import org.json.JSONObject;

import qrom.component.log.QRomLog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * @author baodingzhou
 */

public class OrderManager implements IOrderManager, IOrderManagerInner, IEnvManagerListener {

    private static final String TAG = "OrderManager";

    private static volatile IOrderManager sInstance = null;

    private ArrayList<IOrderManagerListener> mListeners = new ArrayList<IOrderManagerListener>();

    private IEnvManager mEnvManager = null;

    private Object mOrderMapLocked = new Object();

    /**
     * TradeNo --> Order
     */
    private HashMap<String, IOrder> mTradeNoOrderMap = new HashMap<String, IOrder>();

    /**
     * AID --> TradeNO
     */
    private HashMap<String, ArrayList<String>> mAIDTradeNoMap = new HashMap<String, ArrayList<String>>();

    private long mLastSyncOrderMillis = Long.MAX_VALUE;

    private long mLastSyncConfigMillis = Long.MAX_VALUE;

    private OrderManager() {

        mEnvManager = EnvManager.getInstance();
        mEnvManager.registerEnvManagerListener(this);

        mCurrentTrafficConfigStep = mTConfigUnavaiable;
        mCurrentSyncOrderStep = mOrderUnavaiable;

        if (mEnvManager.isCPLCReady()) {
            syncOrder(false);
            syncTrafficConfig(false);
        }
    }

    public static IOrderManager getInstance() {

        if (sInstance == null) {
            synchronized (IOrderManager.class) {
                if (sInstance == null) {
                    sInstance = new OrderManager();
                }
            }
        }

        return sInstance;
    }

    public static IOrderManagerInner getInstanceInner() {
        return (IOrderManagerInner) getInstance();
    }

    @Override
    public boolean registerOrderManagerListener(IOrderManagerListener listener) {
        if (listener == null) {
            return false;
        }

        synchronized (mListeners) {
            if (!mListeners.contains(listener)) {
                return mListeners.add(listener);
            }
        }
        return false;
    }

    @Override
    public boolean unregisterOrderManagerListener(IOrderManagerListener listener) {
        if (listener == null) {
            return false;
        }

        synchronized (mListeners) {
            return mListeners.remove(listener);
        }
    }

    @Override
    public boolean onWatchConnection(boolean connected) {
        return false;
    }

    @Override
    public boolean onWatchIdentified(boolean succeed, String cplc) {
        if (succeed) {
            syncOrder(false);
            syncTrafficConfig(false);
        }
        return false;
    }

    @Override
    public boolean onNewWatchPaired() {
        setSyncOrderStep(mOrderUnavaiable);
        setTrafficConfigStep(mTConfigUnavaiable);
        return false;
    }

    @Override
    public boolean onOldWatchUnpaired() {
        return false;
    }

    private IStep<COMMON_STEP> mCurrentSyncOrderStep = null;

    private final boolean setSyncOrderStep(IStep<COMMON_STEP> step) {

        boolean handle = false;

        if (step == null) {
            return handle;
        }

        if (mCurrentSyncOrderStep != step) {
            IStep<COMMON_STEP> previousStep = mCurrentSyncOrderStep;
            mCurrentSyncOrderStep = step;
            if (previousStep != null) {
                previousStep.onQuitStep();
            }
            mCurrentSyncOrderStep.onEnterStep();
            handle = true;
        }

        return handle;
    }

    private abstract class SyncOrderStep extends Step<COMMON_STEP> {

        private long mUniqueReq = -1;

        public SyncOrderStep(COMMON_STEP step) {
            super(step);

            // Initialized status
            if (step == COMMON_STEP.UNAVAILABLE) {
                mStatus = STATUS.KEEP;
            }
        }

        @Override
        protected boolean setStep(IStep<COMMON_STEP> step) {
            return setSyncOrderStep(step);
        }

        @Override
        protected void notifyStepStatus(COMMON_STEP step, STATUS status) {
            notifyOrderModuleStatus(MODULE.ORDER_LIST, step, status);
        }

        private boolean updateOrders(GetCardStatusRsp rsp) {
            ArrayList<IOrder> mLocalOrderList = new ArrayList<IOrder>();
            synchronized (mOrderMapLocked) {

                Iterator<IOrder> iterator = mTradeNoOrderMap.values().iterator();
                IOrder order = null;
                if (iterator != null) {
                    while (iterator.hasNext()) {
                        order = iterator.next();
                        if (order.isLocal()) {
                            mLocalOrderList.add(order);
                        }
                    }
                }

                mTradeNoOrderMap.clear();
                mAIDTradeNoMap.clear();

                ArrayList<BusCardStatusInfo> unishedOrderList = rsp.getVStatusList();
                if (unishedOrderList != null) {
                    Iterator<BusCardStatusInfo> it = unishedOrderList.iterator();
                    BusCardStatusInfo statusInfo = null;
                    if (it != null) {
                        while (it.hasNext()) {
                            statusInfo = it.next();

                            if (statusInfo.stOrderParam == null
                                    || statusInfo.stOrderParam.stBusCardBaseInfo == null
                                    || statusInfo.stOrderRspParam == null
                                    || statusInfo.stPayResultParam == null) {
                                continue;
                            }
                            order = new Order(statusInfo);
                            mTradeNoOrderMap.put(statusInfo.stOrderRspParam.sTradeNo, order);

                            ArrayList<String> tradeNos = mAIDTradeNoMap
                                    .get(statusInfo.stOrderParam.stBusCardBaseInfo.sInstanceAId);
                            if (tradeNos == null) {
                                tradeNos = new ArrayList<String>();
                                mAIDTradeNoMap.put(
                                        statusInfo.stOrderParam.stBusCardBaseInfo.sInstanceAId,
                                        tradeNos);
                            }
                            tradeNos.add(statusInfo.stOrderRspParam.sTradeNo);
                        }
                    }
                }

                // Local order
                if (!mLocalOrderList.isEmpty()) {
                    iterator = mLocalOrderList.iterator();
                    if (iterator != null) {
                        while (iterator.hasNext()) {
                            order = iterator.next();
                            // 失败订单看远程，成功订单当前只相信本地
                            if (order.getOrderStep() != ORDER_STEP.ORDER_FINISH) {
                                continue;
                            }
                            String tradeNo = order.getOrderRspParam().sTradeNo;
                            String aid = order.getOrderReqParam()
                                    .getStBusCardBaseInfo().sInstanceAId;
                            // 对于此处以本地订单信息为准
//                            if (!mTradeNoOrderMap.containsKey(tradeNo)) {
                                mTradeNoOrderMap.put(tradeNo, order);
                                ArrayList<String> tradeNos = mAIDTradeNoMap.get(aid);
                                if (tradeNos == null) {
                                    tradeNos = new ArrayList<String>();
                                    mAIDTradeNoMap.put(aid, tradeNos);
                                }
                                tradeNos.add(tradeNo);
//                            }
                        }
                    }
                }
            }

            return true;
        }

        protected void syncOrder() {
            mLastSyncOrderMillis = System.currentTimeMillis();
            LatestCardStatus cardStatus = new LatestCardStatus();
            mUniqueReq = cardStatus.getUniqueSeq();
            boolean handled = cardStatus.invoke(new IResponseObserver() {
                @Override
                public void onResponseSucceed(long uniqueSeq, int operType, JceStruct response) {
                    if (mUniqueReq == uniqueSeq) {
                        GetCardStatusRsp rsp = (GetCardStatusRsp) response;
                        if (rsp != null) {
                            if (rsp.iRet == 0) {
                                updateOrders(rsp);
                                switchStep(mOrderUpdate);
                            } else {
                                keepStep();
                            }
                        } else {
                            keepStep();
                        }
                    }
                }

                @Override
                public void onResponseFailed(long uniqueSeq, int operType, int errorCode,
                        String description) {
                    if (mUniqueReq == uniqueSeq) {
                        keepStep();
                    }
                }
            });

            if (!handled) {
                keepStep();
            }
        }
    }

    private final SyncOrderStep mOrderUnavaiable = new SyncOrderStep(COMMON_STEP.UNAVAILABLE) {

        @Override
        public void onStepHandle() {
            syncOrder();
        }
    };

    private final SyncOrderStep mOrderUpdate = new SyncOrderStep(COMMON_STEP.UPDATED) {

        @Override
        public void onStepHandle() {
            switchStep(mOrderReady);
        }
    };

    private final SyncOrderStep mOrderReady = new SyncOrderStep(COMMON_STEP.READY) {
        @Override
        public void onStepHandle() {
            // Do things.
        }
    };

    private final SyncOrderStep mOrderDubious = new SyncOrderStep(COMMON_STEP.DUBIOUS) {
        @Override
        public void onStepHandle() {
            syncOrder();
        }
    };

    private PayBusCardConfigRsp mPayBusCardConfigRsp = null;

    private IStep<COMMON_STEP> mCurrentTrafficConfigStep = null;

    private final boolean setTrafficConfigStep(IStep<COMMON_STEP> step) {

        boolean handle = false;

        if (step == null) {
            return handle;
        }

        if (mCurrentTrafficConfigStep != step) {
            IStep<COMMON_STEP> previousStep = mCurrentTrafficConfigStep;
            mCurrentTrafficConfigStep = step;
            if (previousStep != null) {
                previousStep.onQuitStep();
            }
            mCurrentTrafficConfigStep.onEnterStep();
            handle = true;
        }

        return handle;
    }

    private abstract class TrafficConfigStep extends Step<COMMON_STEP> {
        private long mUniqueReq = -1;

        public TrafficConfigStep(COMMON_STEP step) {
            super(step);

            // Initialized status
            if (step == COMMON_STEP.UNAVAILABLE) {
                mStatus = STATUS.KEEP;
            }
        }

        @Override
        protected boolean setStep(IStep<COMMON_STEP> step) {
            return OrderManager.this.setTrafficConfigStep(step);
        }

        @Override
        protected void notifyStepStatus(COMMON_STEP step, STATUS status) {
            notifyOrderModuleStatus(MODULE.TRAFFIC_CONFIG, step, status);
        }

        protected void syncTrafficConfig() {
            QRomLog.d(TAG, "syncTrafficConfig");
            mLastSyncConfigMillis = System.currentTimeMillis();
            BusCardConfig busCardConfig = new BusCardConfig();
            mUniqueReq = busCardConfig.getUniqueSeq();
            if (mPayBusCardConfigRsp != null) {
                busCardConfig.setmLastMD5(mPayBusCardConfigRsp.sMd5);
            }
            boolean handled = busCardConfig.invoke(new IResponseObserver() {
                @Override
                public void onResponseSucceed(long uniqueSeq, int operType, JceStruct response) {
                    if (mUniqueReq == uniqueSeq) {
                        PayBusCardConfigRsp config = (PayBusCardConfigRsp) response;

                        // 后台的返回规律...
                        if (config != null && (config.iRet == 1 || config.iRet == 0)
                                && config.sMd5 != null) {
                            if (mPayBusCardConfigRsp != null) {
                                if (mPayBusCardConfigRsp.sMd5 != null
                                        && mPayBusCardConfigRsp.sMd5
                                                .equalsIgnoreCase(config.sMd5)) {
                                    switchStep(mTConfigReady);
                                } else {
                                    mPayBusCardConfigRsp = config;
                                    savePayConfig(config);
                                    switchStep(mTConfigUpdated);
                                }
                            } else {
                                mPayBusCardConfigRsp = config;
                                savePayConfig(config);
                                switchStep(mTConfigUpdated);
                            }
                        } else {
                            keepStep();
                        }
                    }
                }

                @Override
                public void onResponseFailed(long uniqueSeq, int operType, int errorCode,
                        String description) {
                    if (mUniqueReq == uniqueSeq) {
                        keepStep();
                    }
                }
            });

            if (!handled) {
                QRomLog.d(TAG, "invoke BusCardConfig error");
                keepStep();
            }
        }
    }

    private final TrafficConfigStep mTConfigUnavaiable = new TrafficConfigStep(
            COMMON_STEP.UNAVAILABLE) {
        @Override
        public void onStepHandle() {
            syncTrafficConfig();
        }
    };

    private final TrafficConfigStep mTConfigUpdated = new TrafficConfigStep(
            COMMON_STEP.UPDATED) {
        @Override
        public void onStepHandle() {
            switchStep(mTConfigReady);
        }
    };

    private final TrafficConfigStep mTConfigReady = new TrafficConfigStep(
            COMMON_STEP.READY) {
        @Override
        public void onStepHandle() {
            // Do nothings.
        }
    };

    private final TrafficConfigStep mTConfigDubious = new TrafficConfigStep(
            COMMON_STEP.DUBIOUS) {
        @Override
        public void onStepHandle() {
            syncTrafficConfig();
        }
    };

    private void syncOrder(boolean force) {
        QRomLog.d(TAG, String.format("syncOrder %b %s %s", force, mCurrentSyncOrderStep.getStep(),
                mCurrentSyncOrderStep.getStatus()));
        if (!mOrderReady.isCurrentStep()) {
            mCurrentSyncOrderStep.onStep();
        } else if (force) {
            if (System.currentTimeMillis()
                    - mLastSyncOrderMillis > Constants.WALLET_ORDER_SYNC_DURATION) {
                setSyncOrderStep(mOrderDubious);
            }
        }
    }

    private void syncTrafficConfig(boolean force) {
        QRomLog.d(
                TAG,
                String.format("syncTrafficConfig %b %s %s", force,
                        mCurrentTrafficConfigStep.getStep(),
                        mCurrentTrafficConfigStep.getStatus()));
        if (!mTConfigReady.isCurrentStep()) {
            mCurrentTrafficConfigStep.onStep();
        } else if (force) {
            if (System.currentTimeMillis()
                    - mLastSyncConfigMillis > Constants.WALLET_CONFIG_SYNC_DURATION) {
                setTrafficConfigStep(mTConfigDubious);
            }
        }
    }

    @Override
    public PayConfig getPayConfig(String aid) {

        if (aid == null || aid.length() == 0) {
            return null;
        }
        if (!isTrafficConfigReady()) {
            return null;
        }

        ArrayList<PayConfig> configs = mPayBusCardConfigRsp.getVConfigList();
        if (configs != null) {
            Iterator<PayConfig> iterator = configs.iterator();
            PayConfig config = null;
            while (iterator.hasNext()) {
                config = iterator.next();
                if (aid.equalsIgnoreCase(config.sInstanceAId)) {
                    return config;
                }
            }
        }

        return null;
    }

    private void addOrderInner(IOrderInner order) {
        if (order == null) {
            return;
        }
        synchronized (mOrderMapLocked) {
            String aid = order.getOrderReqParam().stBusCardBaseInfo.sInstanceAId;
            String tradeNo = order.getOrderRspParam().sTradeNo;

            if (!mTradeNoOrderMap.containsKey(tradeNo)) {
                mTradeNoOrderMap.put(tradeNo, order);
                ArrayList<String> tradeNos = mAIDTradeNoMap.get(aid);
                if (tradeNos == null) {
                    tradeNos = new ArrayList<String>();
                    mAIDTradeNoMap.put(aid, tradeNos);
                }
                tradeNos.add(tradeNo);
            }
        }
    }

    private void clearOrderInner(IOrderInner order) {
        if (order == null) {
            return;
        }
        synchronized (mOrderMapLocked) {
            String aid = order.getOrderReqParam().stBusCardBaseInfo.sInstanceAId;
            String tradeNo = order.getOrderRspParam().sTradeNo;
            if (mAIDTradeNoMap.containsKey(aid)) {
                mAIDTradeNoMap.remove(aid);
            }
            if (mTradeNoOrderMap.containsKey(tradeNo)) {
                mTradeNoOrderMap.remove(tradeNo);
            }
        }
    }

    @Override
    public IOrder[] getOrders(String aid) {
        ArrayList<String> aids = null;
        synchronized (mOrderMapLocked) {
            aids = mAIDTradeNoMap.get(aid);
        }

        if (aids != null && aids.size() > 0) {
            return aids.toArray(new Order[0]);
        }

        return null;
    }

    @Override
    public IOrder getLastOrder(String aid) {
        ArrayList<String> tradeNos = null;
        synchronized (mOrderMapLocked) {
            tradeNos = mAIDTradeNoMap.get(aid);
            if (tradeNos != null && tradeNos.size() > 0) {
                return getOrder(tradeNos.get(tradeNos.size() - 1));
            }
        }
        return null;
    }

    @Override
    public IOrder getOrder(String tradeNo) {
        synchronized (mOrderMapLocked) {
            return mTradeNoOrderMap.get(tradeNo);
        }
    }

    @Override
    public IOrderInner getOrderInner(String tradeNo) {
        return (IOrderInner) getOrder(tradeNo);
    }

    @Override
    public final long placeIssueOrder(final String aid, final int payScene, final int payType, final long activateFee,
            final long chargeValue, final boolean retry) {
        final long uniqueId = SeqGenerator.getInstance().uniqueSeq();
        // 执行业务时，需清空所有的工作线程，避免阻塞
        Utils.getWorkerHandler().removeCallbacksAndMessages(null);
        Utils.getWorkerHandler().post(new Runnable() {
            @Override
            public void run() {
                placeOrderInner(uniqueId, aid, payScene, payType, activateFee,
                        chargeValue, retry);
            }
        });

        return uniqueId;
    }

    @Override
    public final long placeTopupOrder(final String aid,final int payScene, final int payType,
            final long chargeValue, final boolean retry) {
        final long uniqueId = SeqGenerator.getInstance().uniqueSeq();
        // 执行业务时，需清空所有的工作线程，避免阻塞
        Utils.getWorkerHandler().removeCallbacksAndMessages(null);
        Utils.getWorkerHandler().post(new Runnable() {
            @Override
            public void run() {
                placeOrderInner(uniqueId, aid, payScene, payType,
                        0, chargeValue, retry);
            }
        });

        return uniqueId;
    }

    private void placeOrderInner(long uniqueID, String aid, int scene, int payType,
            long activateFee,
            long chargeValue, boolean retry) {

        ICardManager cardManager = CardManager.getInstance();
        ITrafficCard card = (ITrafficCard) cardManager.getCard(aid);
        QRomLog.i(TAG, "placeOrderInner,retry:" + retry);
        if (card == null) {
            QRomLog.e(TAG, "placeIssueOrderInner can not get car for aid:" + aid);
            notifyNewOrder(uniqueID, false, null);
            return;
        }

        BusCardBaseInfo busCardBaseInfo = card.getBusCardBaseInfo();
        PayConfig payConfig = getPayConfig(aid);
        if (busCardBaseInfo == null || payConfig == null) {
            QRomLog.e(TAG, "placeIssueOrderInner can not get BusCardBaseInfo,or payconfig");
            notifyNewOrder(uniqueID, false, null);
            return;
        }
        IOrder order = getLastOrder(aid);
        if (retry) {
            if (order != null) {
                if (((IOrderInner) order).onRetry() < 0) {
                    notifyNewOrder(uniqueID, false, null);
                }
            } else {
                QRomLog.e(TAG, "retry order,but order is null,return");
                notifyNewOrder(uniqueID, false, null);
            }
            return;
        }
        if (order != null) {
            clearOrder((IOrderInner) order);
        }

        OrderReqParam orderReqParam = new OrderReqParam();
        orderReqParam.setSBody(card.getCardName());
        orderReqParam.setSDetail(card.getCardName());
        orderReqParam.setEPayScene(scene);
        if (scene == E_PAY_SCENE._EPS_OPEN_CARD || scene == E_PAY_SCENE._EPS_OPEN_CARD_ONLY) {
            orderReqParam.setITotalFee(getFee(activateFee, chargeValue, payConfig));
            orderReqParam.setIOpenCardFee(getFee(activateFee, 0,payConfig));
            orderReqParam.setIOrderType(WalletRunEnv.getVisitChannel());
        } else {
            orderReqParam.setITotalFee(getFee(0, chargeValue, payConfig));// TODO 改成后台配置
            orderReqParam.setIOrderType(WalletRunEnv.getDefaultChannel());
        }
        orderReqParam.setSTradeType(Constants.TRADE_TYPE);
        orderReqParam.setEPayType(payType);
        orderReqParam.setStBusCardBaseInfo(busCardBaseInfo);
        orderReqParam.setSPackageName(Constants.WALLET_PACKAGE_NAME);
        orderReqParam.setIRetryPay(0);
        orderReqParam.setStWalletBaseInfo(EnvManager.getInstanceInner().getWalletBaseInfo());
        // 当前不考虑优惠活动
        orderReqParam.setIActivityFlag((int)payConfig.getIActivityFlag());

        new Order(orderReqParam, uniqueID);
    }

    @Override
    public void notifyNewOrder(long uniqueSeq, boolean succeed, String tradeNo) {
        QRomLog.d(TAG, String.format("notifyNewOrder uniqueSeq:%d succeed:%b tradeNo:%s",
                uniqueSeq, succeed, tradeNo));
        synchronized (mListeners) {
            Iterator<IOrderManagerListener> iterator = mListeners.iterator();
            IOrderManagerListener listener = null;
            if (iterator != null) {
                while (iterator.hasNext()) {
                    listener = iterator.next();
                    listener.onNewOrder(uniqueSeq, succeed, tradeNo);
                }
            }
        }
    }

    @Override
    public void addOrder(IOrderInner order) {
        addOrderInner(order);
    }

    @Override
    public void clearOrder(IOrderInner order) {
        clearOrderInner(order);
    }

    @Override
    public void notifyOrderStatus(String tradeNo, ORDER_STEP step, STATUS status) {
        QRomLog.d(TAG, String.format("notifyOrderStatus tradeNo:%s step:%s status:%s", tradeNo,
                step, status));
        synchronized (mListeners) {
            Iterator<IOrderManagerListener> iterator = mListeners.iterator();
            IOrderManagerListener listener = null;
            if (iterator != null) {
                while (iterator.hasNext()) {
                    listener = iterator.next();
                    listener.onOrderStatus(tradeNo, step, status);
                }
            }
        }
    }

    @Override
    public void notifyOrderModuleStatus(MODULE module, COMMON_STEP step, STATUS status) {
        QRomLog.d(TAG, String.format("module:%s step:%s status:%s", module, step, status));
        synchronized (mListeners) {
            Iterator<IOrderManagerListener> iterator = mListeners.iterator();
            IOrderManagerListener listener = null;
            if (iterator != null) {
                while (iterator.hasNext()) {
                    listener = iterator.next();
                    listener.onOrderManager(module, step, status);
                }
            }
        }
    }

    @Override
    public void setOrderLocalPaidStatus(final String tradeNo, final boolean paid) {
        // 执行业务时，需清空所有的工作线程，避免阻塞
        Utils.getWorkerHandler().removeCallbacksAndMessages(null);
        Utils.getWorkerHandler().post(new Runnable() {
            @Override
            public void run() {
                QRomLog.d(TAG,
                        String.format("setOrderLocalPaidStatus tradeNo:%s paid:%b", tradeNo, paid));
                IOrderInner order = getOrderInner(tradeNo);
                if (order != null) {
                    order.setLocalPaid(paid);
                } else {
                    QRomLog.e(TAG, "Can not get order for tradeNo:" + tradeNo);
                }
            }
        });
    }

    @Override
    public boolean isTrafficConfigReady() {
        return mTConfigReady.isCurrentStep();
    }

    @Override
    public boolean forceSyncTrafficConfig(final boolean isForce) {

        return Utils.getWorkerHandler().post(new Runnable() {
            @Override
            public void run() {
                syncTrafficConfig(isForce);
            }
        });
    }

    @Override
    public boolean isOrderReady() {
        QRomLog.d(TAG, String.format("isOrderReady %s %s", mCurrentSyncOrderStep.getStep(),
                mCurrentSyncOrderStep.getStatus()));
        return mOrderReady.isCurrentStep();
    }

    @Override
    public boolean forceSyncOrder(final boolean isForce) {
        QRomLog.d(TAG, "forceSyncOrder");
        return Utils.getWorkerHandler().post(new Runnable() {
            @Override
            public void run() {
                syncOrder(isForce);
            }
        });
    }

    @Override
    public boolean isInOrderSyncProcess() {
        STATUS curStatus = mCurrentSyncOrderStep.getStatus();
        COMMON_STEP curStep = mCurrentSyncOrderStep.getStep();
        if (curStep == COMMON_STEP.READY) {
            return false;
        }
        return curStatus != STATUS.KEEP && curStatus != STATUS.QUIT;
    }

    private long getFee(long actFee, long charFee,PayConfig config) {
        boolean isTest = false;//ServerHandler.getInstance().isTestEnv();
        long activityAmount = 0;
        if (actFee > 0) {
            activityAmount = config.getIActivityFlag() == 1 ? config.iActivityAmount : 0;
        } else {
            ArrayList<PayRechargeAmount> vAmount = config.getVPayRechargeAmountList();
            if (config.getIRechargeActivityFlag() == 1 && vAmount != null) {
                for (PayRechargeAmount tmAmount : vAmount) {
                    if (charFee == tmAmount.iTotalFee) {
                        activityAmount = tmAmount.iActivityAmount;
                        break;
                    }
                }
            }
        }
        long retFee_release = actFee + charFee - activityAmount;
        long retFee_test = (charFee == 0) ? actFee : (retFee_release / 5000);
        return isTest ? retFee_test : retFee_release;
    }

    private void savePayConfig(PayBusCardConfigRsp rsp) {
        Utils.clearPayConfigs();
        ArrayList<PayConfig> list = rsp.vConfigList;
        Utils.enableWalletMoudle(list != null && !list.isEmpty());
        StringBuilder builder = new StringBuilder();
        int count = 0;
        for (PayConfig payConfig : list) {
            if (!TextUtils.isEmpty(payConfig.sInstanceAId)) {
                builder.append(payConfig.sInstanceAId).append(",");
                count++;
            }
        }
        Utils.saveWhiteList2Cache(count, builder.toString());
        sendCacheListToWatch(builder.toString());
    }

    private void sendCacheListToWatch(String data) {
        if (TextUtils.isEmpty(data)) {
            return;
        }
        JSONObject object = new JSONObject();
        try {
            object.put(Constants.WALLET_WHITELIST_KEY, data);
            PassData passdata = new PassData();
            passdata.putString(object.toString());
            passdata.invoke(new IResult() {

                @Override
                public void onResult(long seqID, int ret, String[] outputParams,
                        Integer[] resultCode,
                        byte[] bytes) {
                }

                @Override
                public void onExecption(long seqID, int error) {
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
