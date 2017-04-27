
package com.pacewear.tws.phoneside.wallet.ui2.activity;

import com.pacewear.tws.phoneside.wallet.R;
import com.pacewear.tws.phoneside.wallet.card.ICard;
import com.pacewear.tws.phoneside.wallet.card.ICard.INSTALL_STATUS;
import com.pacewear.tws.phoneside.wallet.order.IOrder;
import com.pacewear.tws.phoneside.wallet.order.IOrderManager.ORDER_STEP;

import java.util.ArrayList;
import java.util.List;

import TRom.E_PAY_SCENE;

public class BusinessResultHandler {
    // 开卡成功
    private ResultFilter mIssueSuccess = new ResultFilter(0, R.string.activate_card_succeed) {

        @Override
        boolean onFilter(BusinessContext context) {
            return context.invokeResult == 0 && context.isTopupInvoke == false;
        }
    };
    // 充值成功
    private ResultFilter mTopupSuccess = new ResultFilter(0,
            R.string.wallet_operation_charge_succeed) {

        @Override
        boolean onFilter(BusinessContext context) {
            // TODO Auto-generated method stub
            return context.invokeResult == 0 && context.isTopupInvoke == true;
        }
    };
    // 开卡成功，但充值失败
    private ResultFilter mIssueSuc_TopupFail = new ResultFilter(-1,
            R.string.wallet_activate_card_topup_failed) {

        @Override
        boolean onFilter(BusinessContext context) {
            boolean hasPersonal = context.card.getInstallStatus() == INSTALL_STATUS.PERSONAL;
            if (context.order == null) {
                return false;
            }
            ORDER_STEP localStep = context.order.getOrderStep();
            return !context.isTopupInvoke && hasPersonal && localStep == ORDER_STEP.EXECUTE_TOPUP;
        }
    };
    // 开卡成功，但查询失败
    private ResultFilter mIssueSuc_QueryFail = new ResultFilter(1,
            R.string.wallet_activate_card_query_failed) {

        @Override
        boolean onFilter(BusinessContext context) {
            if (context.order == null) {
                return false;
            }
            ORDER_STEP localStep = context.order.getOrderStep();
            boolean isOrderFinish = (localStep == ORDER_STEP.ORDER_FINISH);
            return !context.isTopupInvoke && isOrderFinish && context.invokeResult != 0;
        }
    };
    // 充值成功，但查询失败
    private ResultFilter mTopupSuc_QueryFail = new ResultFilter(1,
            R.string.wallet_topup_card_query_failed) {

        @Override
        boolean onFilter(BusinessContext context) {
            if (context.order == null) {
                return false;
            }
            ORDER_STEP localStep = context.order.getOrderStep();
            boolean isOrderFinish = (localStep == ORDER_STEP.ORDER_FINISH);
            return context.isTopupInvoke && isOrderFinish && context.invokeResult != 0;
        }
    };
    // 开卡失败
    private ResultFilter mIssueFail = new ResultFilter(-1, R.string.wallet_activate_card_failed) {

        @Override
        boolean onFilter(BusinessContext context) {
            if (context.isTopupInvoke) {
                return false;
            }
            if (context.order == null) {
                setToast(R.string.wallet_no_order);
                return true;
            }
            if (context.order.isInValidOrder()) {
                setToast(R.string.wallet_invalid_order);
                return true;
            }
            return context.invokeResult != 0;
        }
    };
    // 充值失败
    private ResultFilter mTopupFail = new ResultFilter(-1,
            R.string.wallet_operation_charge_failed) {

        @Override
        boolean onFilter(BusinessContext context) {
            if (!context.isTopupInvoke) {
                return false;
            }
            if (context.order == null) {
                setToast(R.string.wallet_no_order);
                return true;
            }
            if (context.order.isInValidOrder()) {
                setToast(R.string.wallet_invalid_order);
                return true;
            }
            return context.invokeResult != 0;
        }
    };

    public void invoke(BusinessContext context) {
        ResultFilterChain chain = new ResultFilterChain();
        chain.add(mIssueSuccess);
        chain.add(mTopupSuccess);
        chain.add(mIssueSuc_TopupFail);
        chain.add(mIssueSuc_QueryFail);
        chain.add(mTopupSuc_QueryFail);
        chain.add(mIssueFail);
        chain.add(mTopupFail);
        chain.invoke(context);
    }

    public static class ResultFilterChain {
        private List<ResultFilter> mList = new ArrayList<ResultFilter>();
        private int mSize = 0;

        void add(ResultFilter node) {
            if (node == null) {
                return;
            }
            synchronized (ResultFilter.class) {
                if (mSize > 0) {
                    mList.get(mSize - 1).setNext(node);
                }
                mList.add(node);
                mSize++;
            }
        }

        final Result invoke(BusinessContext context) {
            if (mList.size() <= 0) {
                return null;
            }
            return mList.get(0).filter(context);
        }
    }

    public static abstract class ResultFilter {
        private ResultFilter mNextFilter = null;
        private Result mResult = null;

        public ResultFilter(int finish, int resTitle) {
            mResult = new Result(finish, resTitle);
        }

        public void setNext(ResultFilter next) {
            mNextFilter = next;
        }

        public final Result filter(BusinessContext context) {
            if (onFilter(context)) {
                return mResult;
            }
            return mNextFilter != null ? mNextFilter.filter(context) : null;
        }

        protected final boolean isTopupScene(IOrder order) {
            return order.getOrderReqParam().getEPayScene() == E_PAY_SCENE._EPS_STAT;
        }

        abstract boolean onFilter(BusinessContext context);

        protected final void setToast(int resToast) {
            mResult.setToast(resToast);
        }
    }

    public static class Result {
        private int iFinish;
        private int iTitleRes;
        private int iToastRes;

        public Result(int finsh, int resTitle) {
            iFinish = finsh;
            iTitleRes = resTitle;
        }

        public void setToast(int toast) {
            iToastRes = toast;
        }
    }

    public static class BusinessContext {
        public ICard card;
        public IOrder order;
        public boolean isTopupInvoke;
        public int invokeResult;
        public String aid;
    }
}
