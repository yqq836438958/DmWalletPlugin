
package com.pacewear.tws.phoneside.wallet.ui2.activity;

import android.content.Context;
import android.text.TextUtils;

import com.pacewear.tws.phoneside.wallet.R;
import com.pacewear.tws.phoneside.wallet.card.ICard;
import com.pacewear.tws.phoneside.wallet.card.ITrafficCard;
import com.pacewear.tws.phoneside.wallet.card.ICard.INSTALL_STATUS;
import com.pacewear.tws.phoneside.wallet.card.ICardInner.CONFIG;
import com.pacewear.tws.phoneside.wallet.common.Utils;
import com.pacewear.tws.phoneside.wallet.order.IOrder;
import com.pacewear.tws.phoneside.wallet.order.IOrderManager.ORDER_STEP;

import java.util.ArrayList;
import java.util.List;

public class BusinessResultHandler {
    private Context mContext = null;

    public BusinessResultHandler(Context context) {
        mContext = context;
    }

    // 开卡成功
    private ResultFilter mIssueSuccess = new ResultFilter() {

        @Override
        public boolean onFilter(BusinessContext context) {
            return context.invokeResult == 0 && context.isTopupInvoke == false;
        }

        @Override
        public void onHandle(Result result, BusinessContext context) {
            result.setTitle(R.string.activate_card_succeed);
            String issueSuc = mContext.getString(R.string.wallet_issuecard_sucess);
            issueSuc = issueSuc.replace("#", context.card.getCardName());
            result.setDescRes(issueSuc);
        }

    };
    // 充值成功
    private ResultFilter mTopupSuccess = new ResultFilter() {

        @Override
        public boolean onFilter(BusinessContext context) {
            return context.invokeResult == 0 && context.isTopupInvoke == true;
        }

        @Override
        public void onHandle(Result result, BusinessContext context) {
            result.setTitle(R.string.wallet_operation_charge_succeed);
            result.setDescRes(getChargeBalanceTips(context.card));
        }
    };
    // 开卡成功，但充值失败 R.string.wallet_activate_card_topup_failed
    private ResultFilter mIssueSuc_TopupFail = new ResultFilter() {

        @Override
        public boolean onFilter(BusinessContext context) {
            boolean hasPersonal = context.card.getInstallStatus() == INSTALL_STATUS.PERSONAL;
            if (context.order == null) {
                return false;
            }
            ORDER_STEP localStep = context.order.getOrderStep();
            return !context.isTopupInvoke && hasPersonal && localStep == ORDER_STEP.EXECUTE_TOPUP;
        }

        @Override
        public void onHandle(Result result, BusinessContext context) {
            // result.setTitle(R.string.wallet_activate_card_topup_failed); TODO 产品设计需求，这里不显示具体
            result.setTitle(R.string.wallet_activate_card_failed);
            showFailDesc(result, context);
        }
    };
    // 开卡成功，但查询失败
    private ResultFilter mIssueSuc_QueryFail = new ResultFilter() {

        @Override
        public boolean onFilter(BusinessContext context) {
            if (context.order == null) {
                return false;
            }
            ORDER_STEP localStep = context.order.getOrderStep();
            boolean isOrderFinish = (localStep == ORDER_STEP.ORDER_FINISH);
            return !context.isTopupInvoke && isOrderFinish && context.invokeResult != 0;
        }

        @Override
        public void onHandle(Result result, BusinessContext context) {
            result.setTitle(R.string.wallet_activate_card_query_failed);
            result.setDescRes(mContext.getString(R.string.wallet_operation_failed_tip));
        }
    };
    // 充值成功，但查询失败 R.string.wallet_topup_card_query_failed
    private ResultFilter mTopupSuc_QueryFail = new ResultFilter() {

        @Override
        public boolean onFilter(BusinessContext context) {
            if (context.order == null) {
                return false;
            }
            ORDER_STEP localStep = context.order.getOrderStep();
            boolean isOrderFinish = (localStep == ORDER_STEP.ORDER_FINISH);
            return context.isTopupInvoke && isOrderFinish && context.invokeResult != 0;
        }

        @Override
        public void onHandle(Result result, BusinessContext context) {
            result.setTitle(R.string.wallet_topup_card_query_failed);
            result.setDescRes(mContext.getString(R.string.wallet_operation_failed_tip));
        }
    };
    // 开卡失败
    private ResultFilter mIssueFail = new ResultFilter() {

        @Override
        public boolean onFilter(BusinessContext context) {
            if (context.isTopupInvoke) {
                return false;
            }
            return context.order == null || context.order.isInValidOrder() ||
                    context.invokeResult != 0;
        }

        @Override
        public void onHandle(Result result, BusinessContext context) {
            result.setTitle(R.string.wallet_activate_card_failed);
            showFailDesc(result, context);
            showToastIfNeed(result, context.order);
        }
    };
    // 充值失败
    private ResultFilter mTopupFail = new ResultFilter() {

        @Override
        public boolean onFilter(BusinessContext context) {
            if (!context.isTopupInvoke) {
                return false;
            }
            return context.order == null || context.order.isInValidOrder() ||
                    context.invokeResult != 0;
        }

        @Override
        public void onHandle(Result result, BusinessContext context) {
            result.setTitle(R.string.wallet_operation_charge_failed);
            showFailDesc(result, context);
            showToastIfNeed(result, context.order);
        }
    };

    public Result invoke(BusinessContext context) {
        List<ResultFilter> filterList = new ArrayList<BusinessResultHandler.ResultFilter>();
        Result target = new Result();
        filterList.add(mIssueSuccess);
        filterList.add(mTopupSuccess);
        filterList.add(mIssueSuc_TopupFail);
        filterList.add(mIssueSuc_QueryFail);
        filterList.add(mTopupSuc_QueryFail);
        filterList.add(mIssueFail);
        filterList.add(mTopupFail);
        for (ResultFilter filter : filterList) {
            if (filter.onFilter(context)) {
                filter.onHandle(target, context);
                break;
            }
        }
        return target;
    }

    public static interface ResultFilter {

        boolean onFilter(BusinessContext context);

        void onHandle(Result result, BusinessContext context);
    }

    public static class Result {
        private int iTitleRes;
        private String iDescRes;
        private int iToastRes;

        public Result() {
        }

        void setTitle(int title) {
            iTitleRes = title;
        }

        void setDescRes(String desc) {
            iDescRes = desc;
        }

        void setToast(int toast) {
            iToastRes = toast;
        }

        public int getTitleRes() {
            return iTitleRes;
        }

        public int getToastRes() {
            return iToastRes;
        }

        public String getDescRes() {
            return iDescRes;
        }
    }

    public static class BusinessContext {
        public ICard card;
        public IOrder order;
        public boolean isTopupInvoke;
        public int invokeResult;
    }

    private String getChargeBalanceTips(ICard card) {
        long iCurBalance = getCurCardBalance(card);
        String newbalance = Utils.getDisplayBalance(iCurBalance);
        return String.format(mContext.getString(R.string.wallet_traffic_card_balance_charge),
                newbalance);
    }

    private long getCurCardBalance(ICard card) {
        String strBalance = ((ITrafficCard) card).getBalance();
        if (TextUtils.isEmpty(strBalance)) {
            return 0;
        }
        return Long.parseLong(strBalance);
    }

    private void showFailDesc(Result result, BusinessContext context) {
        if (showErrCode(context)) {
            result.setDescRes(context.order.getBusinessErr());
        } else {
            result.setDescRes(mContext.getString(R.string.wallet_operation_failed_tip));
        }
    }

    private void showToastIfNeed(Result result, IOrder order) {
        if (order == null) {
            result.setToast(R.string.wallet_no_order);
            return;
        }
        if (order.isInValidOrder()) {
            result.setToast(R.string.wallet_invalid_order);
        }
    }

    private boolean showErrCode(BusinessContext context) {
        IOrder order = context.order;
        boolean hasErrCode = (order != null && !TextUtils.isEmpty(order.getBusinessErr()));
        return CONFIG.BEIJINGTONG.mAID.equalsIgnoreCase(context.card.getAID()) && hasErrCode;
    }

}
