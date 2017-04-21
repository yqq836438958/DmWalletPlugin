
package com.pacewear.tws.phoneside.wallet.card;

import android.text.TextUtils;

import com.pacewear.tws.phoneside.wallet.card.ICard.ACTIVATION_STATUS;
import com.pacewear.tws.phoneside.wallet.card.ICard.CARD_TYPE;
import com.pacewear.tws.phoneside.wallet.card.ICard.INSTALL_STATUS;
import com.pacewear.tws.phoneside.wallet.common.Constants;
import com.pacewear.tws.phoneside.wallet.common.Utils;
import com.pacewear.tws.phoneside.wallet.env.EnvManager;
import com.pacewear.tws.phoneside.wallet.env.IEnvManager;
import com.pacewear.tws.phoneside.wallet.env.IEnvManagerListener;
import com.pacewear.tws.phoneside.wallet.step.IStep;
import com.pacewear.tws.phoneside.wallet.step.IStep.COMMON_STEP;
import com.pacewear.tws.phoneside.wallet.step.IStep.STATUS;
import com.pacewear.tws.phoneside.wallet.step.Step;
import com.pacewear.tws.phoneside.wallet.walletservice.CardListQuery;
import com.pacewear.tws.phoneside.wallet.walletservice.IResult;
import com.tencent.tws.phoneside.utils.MD5Util;
import com.tencent.tws.proto.wallet.WatchNFCManager.MSG_STATE;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import qrom.component.log.QRomLog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;

/**
 * @author baodingzhou
 */

public class CardManager implements ICardManager, ICardManagerInner, IEnvManagerListener {

    private static final String TAG = "CardManager";

    private static ICardManager sInstance = null;

    private ArrayList<ICardManagerListener> mListener = new ArrayList<ICardManagerListener>();

    private HashMap<String, ICard> mAIDCardMap = new HashMap<String, ICard>();

    private HashMap<ICard.CARD_TYPE, ArrayList<ICard>> mTypeCollection = new HashMap<ICard.CARD_TYPE, ArrayList<ICard>>();

    protected volatile static String sLastestCardListQueryMD5 = null;

    private IStep<COMMON_STEP> mCurrentCardListStep = null;

    private int mQueryListTimes = 0;

    private final boolean setCardListStep(IStep<COMMON_STEP> step) {
        if (step == null) {
            return false;
        }
        if (mCurrentCardListStep != step) {
            IStep<COMMON_STEP> previousStep = mCurrentCardListStep;
            mCurrentCardListStep = step;
            if (previousStep != null) {
                previousStep.onQuitStep();
            }
            mCurrentCardListStep.onEnterStep();
            return true;
        }
        return false;
    }

    private abstract class CardStep extends Step<COMMON_STEP> {

        private long mUniqueReq = -1;

        public CardStep(COMMON_STEP step) {
            super(step);

            if (step == COMMON_STEP.UNAVAILABLE) {
                mStatus = STATUS.KEEP;
            }
        }

        @Override
        protected final boolean setStep(IStep<COMMON_STEP> step) {
            return CardManager.this.setCardListStep(step);
        }

        @Override
        protected final void notifyStepStatus(COMMON_STEP step, STATUS status) {
            notifyCardListMsg(step, status);
        }

        private final boolean updateCardStatus(String list) {
            QRomLog.d(TAG, "updateCardStatus " + list);

            JSONObject root = null;

            try {
                root = new JSONObject(list);
            } catch (JSONException e) {
                QRomLog.e(TAG, e.getMessage());
                return false;
            }

            if (root.has("card_list")) {

                JSONArray cardInfoArray = null;

                try {
                    cardInfoArray = root.getJSONArray("card_list");
                } catch (JSONException e) {
                    QRomLog.e(TAG, e.getMessage());
                }
                clearAllCards();
                JSONObject cardInfo = null;
                if (cardInfoArray != null) {
                    for (int i = 0; i < cardInfoArray.length(); i++) {
                        try {
                            cardInfo = cardInfoArray.getJSONObject(i);
                        } catch (JSONException e) {
                            QRomLog.e(TAG, e.getMessage());
                        }

                        if (cardInfo == null) {
                            continue;
                        }

                        if (!cardInfo.has("instance_id")) {
                            continue;
                        }

                        String aid = null;
                        try {
                            aid = cardInfo.getString("instance_id");
                        } catch (JSONException e) {
                            QRomLog.e(TAG, e.getMessage());
                        }

                        if (aid == null) {
                            continue;
                        }

                        Card card = (Card) getCard(aid);
                        if (card == null) {
                            continue;
                        }

                        if (cardInfo.has("install_status")) {
                            String installStatus = null;
                            try {
                                installStatus = cardInfo.getString("install_status");
                            } catch (JSONException e) {
                                QRomLog.e(TAG, e.getMessage());
                            }

                            if (installStatus != null) {
                                if (installStatus.equalsIgnoreCase("0")) {
                                    card.setInstallStatus(INSTALL_STATUS.UNINSTALLED);
                                } else if (installStatus.equalsIgnoreCase("1")) {
                                    card.setInstallStatus(INSTALL_STATUS.INSTALLED);
                                } else if (installStatus.equalsIgnoreCase("2")) {
                                    card.setInstallStatus(INSTALL_STATUS.PERSONAL);
                                } else {
                                    // Error. do nothing.
                                }
                            }
                        }

                        if (cardInfo.has("activation_status")) {
                            String activationStatus = null;
                            try {
                                activationStatus = cardInfo.getString("activation_status");
                            } catch (JSONException e) {
                                QRomLog.e(TAG, e.getMessage());
                            }

                            if (activationStatus != null) {
                                if (activationStatus.equalsIgnoreCase("0")) {
                                    card.setActivationStatus(ACTIVATION_STATUS.UNACTIVATED);
                                } else if (activationStatus.equalsIgnoreCase("1")) {
                                    card.setActivationStatus(ACTIVATION_STATUS.ACTIVATED);
                                } else {
                                    // Error. do nothing.
                                }
                            }
                        }
                    }

                    return true;
                }
            }

            return false;
        }

        protected final boolean queryCardList() {
            QRomLog.d(TAG, "queryCardList");
            CardListQuery cardListQuery = new CardListQuery();
            mUniqueReq = cardListQuery.getSeqID();
            return cardListQuery.invoke(new IResult() {

                @Override
                public void onResult(long seqID, int ret, String[] outputParams,
                        Integer[] resultCode, byte[] bytes) {
                    if (mUniqueReq == seqID) {
                        mQueryListTimes = 0;
                        String list = (outputParams != null && outputParams.length > 0)
                                ? outputParams[0] : null;
                        QRomLog.d(TAG,
                                String.format("CardListQuery.onResult ret:%d list:%s", ret, list));
                        if (ret == 0 && list != null) {
                            String md5 = MD5Util.getMD5String(list);
                            if (sLastestCardListQueryMD5 != null
                                    && sLastestCardListQueryMD5.equalsIgnoreCase(md5)) {
                                switchStep(mCardListReady);
                            } else if (updateCardStatus(list)) {
                                switchStep(mCardListUpdated);
                                sLastestCardListQueryMD5 = md5;
                            } else {
                                keepStep();
                            }
                            Utils.saveCacheCardList(list);
                        } else if (ret == Constants.WALLET_ERRCODE_EMPTYLIST) {
                            // 查询成功，但是列表为空
                            sLastestCardListQueryMD5 = null;
                            clearAllCards();
                            switchStep(mCardListReady);
                            Utils.saveCacheCardList("");
                        } else {
                            keepStep();
                        }
                    }
                }

                @Override
                public void onExecption(long seqID, int error) {
                    if (mUniqueReq == seqID) {
                        QRomLog.d(
                                TAG,
                                String.format("CardListQuery.onExecption error:%s",
                                        MSG_STATE.convert(error)));
                        if (error == MSG_STATE._TIMEOUT) {
                            // 如果超时，自动再重试2次，超过重试次数，返回给用户
                            mQueryListTimes++;
                            if (!isOverMaxQueryTimes()) {
                                mCurrentCardListStep.onEnterStep();
                            } else {
                                keepStep();
                            }
                        } else {
                            keepStep();
                        }
                    }
                }
            });
        };
    }

    private final CardStep mCardListUnavaiable = new CardStep(COMMON_STEP.UNAVAILABLE) {
        @Override
        public void onStepHandle() {
            sLastestCardListQueryMD5 = null;
            Utils.saveCacheCardList("");
            queryCardList();
        }
    };

    private final CardStep mCardListUpdated = new CardStep(COMMON_STEP.UPDATED) {
        @Override
        public void onStepHandle() {
            switchStep(mCardListReady);
        }
    };

    private final CardStep mCardListReady = new CardStep(COMMON_STEP.READY) {
        @Override
        public void onStepHandle() {
            // 无条件强制更新各卡片信息
            updateCardsInfo(true);
        }
    };

    private final CardStep mCardListDubious = new CardStep(COMMON_STEP.DUBIOUS) {
        @Override
        public void onStepHandle() {
            queryCardList();
        }
    };

    public static ICardManager getInstance() {
        if (sInstance == null) {
            synchronized (CardManager.class) {
                if (sInstance == null) {
                    sInstance = new CardManager();
                }
            }
        }

        return sInstance;
    }

    public static ICardManagerInner getInstanceInner() {
        return (ICardManagerInner) getInstance();
    }

    public CardManager() {

        ICardInner.CONFIG[] configs = ICardInner.CONFIG.values();
        ICardInner.CONFIG config = null;
        ICard card = null;
        CARD_TYPE type = CARD_TYPE.TRAFFIC_CARD;
        ArrayList<ICard> typeCards = null;
        if (configs != null) {
            for (int i = 0; i < configs.length; i++) {
                config = configs[i];
                card = config.newInstance();
                if (card != null) {

                    // AID --> ICard
                    mAIDCardMap.put(card.getAID(), card);

                    // CARD_TYPE --> ICard
                    type = card.getCardType();
                    typeCards = mTypeCollection.get(type);
                    if (typeCards == null) {
                        typeCards = new ArrayList<ICard>();
                        mTypeCollection.put(type, typeCards);
                    }
                    typeCards.add(card);
                }
            }
        }

        mCurrentCardListStep = mCardListUnavaiable;

        IEnvManager envManager = EnvManager.getInstance();
        envManager.registerEnvManagerListener(this);
    }

    private final void clearAllCards() {
        ICard[] cards = this.getCard();
        for (ICard card : cards) {
            card.clear();
        }
    }

    private final void updateCardsInfo(boolean force) {
        QRomLog.d(TAG, "updateCardsInfo force:" + force);
        ArrayList<ICard> cards = getCard(INSTALL_STATUS.PERSONAL);
        if (cards != null) {
            for (ICard card : cards) {
                if (force) {
                    card.forceUpdate();
                } else if (!card.isReady()) {
                    card.forceUpdate();
                }
            }
        }
    }

    @Override
    public void notifyCardListMsg(COMMON_STEP step, STATUS status) {
        QRomLog.d(TAG, String.format("notifyCardListMsg step:%s status:%s", step, status));
        Iterator<ICardManagerListener> iterator = null;
        ICardManagerListener listener = null;
        synchronized (mListener) {
            iterator = mListener.iterator();
            if (iterator != null) {
                while (iterator.hasNext()) {
                    listener = iterator.next();
                    listener.onCardListMsg(step, status);
                }
            }
        }
    }

    @Override
    public void notifyCardMsg(String aid, COMMON_STEP step, STATUS status) {
        QRomLog.d(TAG, String.format("notifyCardMsg aid:%s step:%s status:%s", aid, step, status));
        Iterator<ICardManagerListener> iterator = null;
        ICardManagerListener listener = null;
        synchronized (mListener) {
            iterator = mListener.iterator();
            if (iterator != null) {
                while (iterator.hasNext()) {
                    listener = iterator.next();
                    listener.onCardMsg(aid, step, status);
                }
            }
        }
    }

    @Override
    public boolean registerCardManagerListener(ICardManagerListener listener) {
        if (listener == null) {
            return false;
        }

        synchronized (mListener) {
            if (!mListener.contains(listener)) {
                return mListener.add(listener);
            }
        }

        return false;
    }

    @Override
    public boolean unregisterCardManagerListener(ICardManagerListener listener) {
        if (listener == null) {
            return false;
        }

        synchronized (mListener) {
            return mListener.remove(listener);
        }
    }

    @Override
    public ICard getCard(String aid) {
        return mAIDCardMap.get(aid);
    }

    @Override
    public ICard[] getCard() {
        return (ICard[]) mAIDCardMap.values().toArray(new Card[0]);
    }

    @Override
    public ArrayList<ICard> getCard(CARD_TYPE type) {
        ArrayList<ICard> cards = mTypeCollection.get(type);
        if (cards == null) {
            return null;
        }
        Collections.sort(cards, new Comparator<ICard>() {

            @Override
            public int compare(ICard card1, ICard card2) {
                if (card1.getActivationStatus() == ACTIVATION_STATUS.ACTIVATED) {
                    return -1;
                } else if (card2.getActivationStatus() == ACTIVATION_STATUS.ACTIVATED) {
                    return 1;
                }
                return 0;
            }
        });

        return cards;
    }

    @Override
    public ArrayList<ICard> getCard(INSTALL_STATUS installStatus) {
        ArrayList<ICard> cards = new ArrayList<ICard>();
        Iterator<ICard> iterator = mAIDCardMap.values().iterator();
        ICard card = null;
        if (iterator != null) {
            while (iterator.hasNext()) {
                card = iterator.next();
                if (card.getInstallStatus() == installStatus) {
                    cards.add(card);
                }
            }
        }

        return cards;
    }

    @Override
    public boolean onWatchConnection(boolean connected) {
        if (connected) {
            update(false);
        }
        return true;
    }

    @Override
    public boolean onWatchIdentified(boolean succeed, String cplc) {
        if (succeed) {
            update(false);
        }
        return true;
    }

    @Override
    public boolean onNewWatchPaired() {
        setCardListStep(mCardListUnavaiable);
        return false;
    }

    @Override
    public boolean onOldWatchUnpaired() {
        // TODO
        return true;
    }

    private void update(boolean force) {
        QRomLog.d(TAG, "update force:" + force);
        mQueryListTimes = 0;
        if (!mCardListReady.isCurrentStep()) {
            mCurrentCardListStep.onStep();
        } else if (force) {
            setCardListStep(mCardListDubious);
        } else {
            if (!TextUtils.isEmpty(Utils.getCacheCardList())) {
                updateCardsInfo(true);
            } else {
                setCardListStep(mCardListDubious);
            }
        }
    }

    @Override
    public boolean forceUpdate(final boolean isForce) {
        QRomLog.d(TAG, "forceUpdate");
        return Utils.getWorkerHandler().post(new Runnable() {
            @Override
            public void run() {
                update(isForce);
            }
        });
    }

    @Override
    public boolean isReady() {
        return mCardListReady.isCurrentStep();
    }

    @Override
    public boolean isAvaliable() {
        return !mCardListUnavaiable.isCurrentStep();
    }

    @Override
    public boolean setDefaultCard(String aid) {
        ICard card = getCard(aid);
        return card.setDefaultCard();
    }

    @Override
    public boolean isOverMaxQueryTimes() {
        return (mQueryListTimes >= Constants.WALLET_QUERY_MAX_TIMES);
    }

    @Override
    public boolean isInSyncProcess() {
        STATUS curStatus = mCurrentCardListStep.getStatus();
        COMMON_STEP curStep = mCurrentCardListStep.getStep();
        if (curStep == COMMON_STEP.READY) {
            return false;
        }
        return curStatus != STATUS.KEEP && curStatus != STATUS.QUIT;
    }

    @Override
    public boolean isEmpty() {
        String list = Utils.getCacheCardList();
        return TextUtils.isEmpty(list);
    }
}
