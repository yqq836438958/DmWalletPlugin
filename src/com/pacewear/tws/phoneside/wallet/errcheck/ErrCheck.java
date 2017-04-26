
package com.pacewear.tws.phoneside.wallet.errcheck;

import com.pacewear.tws.phoneside.wallet.card.CardManager;
import com.pacewear.tws.phoneside.wallet.env.EnvManager;
import com.pacewear.tws.phoneside.wallet.order.OrderManager;

import java.util.ArrayList;
import java.util.List;

public class ErrCheck {

    private List<CheckHandler> mCheckList = new ArrayList<ErrCheck.CheckHandler>();

    public abstract static class CheckHandler {
        private CheckHandler mNext = null;

        public void setNext(CheckHandler next) {
            mNext = next;
        }

        public boolean call() {
            boolean checkEnv = isErrHappen();
            if (checkEnv) {
                onHandle();
                return checkEnv;
            }
            if (mNext != null) {
                return mNext.call();
            }
            return checkEnv;
        }

        protected abstract boolean isErrHappen();

        protected abstract void onHandle();
    }

    public void addCheck(CheckHandler handle) {
        mCheckList.add(handle);
    }

    public boolean invoke() {
        return mCheckList.get(0).call();
    }

    public abstract static class CardManagerNotReady extends CheckHandler {
        @Override
        protected boolean isErrHappen() {
            return !CardManager.getInstance().isReady();
        }
    }

    public abstract static class OrderManagerNotReady extends CheckHandler {
        @Override
        protected boolean isErrHappen() {
            return !OrderManager.getInstance().isOrderReady();
        }
    }

    public abstract static class DeviceNotConnect extends CheckHandler {
        @Override
        protected boolean isErrHappen() {
            return !EnvManager.getInstance().isWatchConnected();
        }
    }

}
