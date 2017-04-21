
package com.pacewear.tws.phoneside.wallet.order;

import com.google.gson.JsonObject;
import com.pacewear.httpserver.IResponseObserver;
import com.pacewear.tws.phoneside.wallet.card.CardManager;
import com.pacewear.tws.phoneside.wallet.card.ICard;
import com.pacewear.tws.phoneside.wallet.card.ICard.INSTALL_STATUS;
import com.pacewear.tws.phoneside.wallet.card.ICardInner;
import com.pacewear.tws.phoneside.wallet.card.ICardInner.CONFIG;
import com.pacewear.tws.phoneside.wallet.common.Constants;
import com.pacewear.tws.phoneside.wallet.common.Utils;
import com.pacewear.tws.phoneside.wallet.env.EnvManager;
import com.pacewear.tws.phoneside.wallet.order.IOrderManager.ORDER_STEP;
import com.pacewear.tws.phoneside.wallet.pay.IPayManagerInner;
import com.pacewear.tws.phoneside.wallet.pay.PayManager;
import com.pacewear.tws.phoneside.wallet.step.IStep;
import com.pacewear.tws.phoneside.wallet.step.IStep.STATUS;
import com.pacewear.tws.phoneside.wallet.step.Step;
import com.pacewear.tws.phoneside.wallet.tosservice.GetPayResult;
import com.pacewear.tws.phoneside.wallet.tosservice.LatestCardStatus;
import com.pacewear.tws.phoneside.wallet.tosservice.UnifiedOrder;
import com.pacewear.tws.phoneside.wallet.walletservice.CardTopUp;
import com.pacewear.tws.phoneside.wallet.walletservice.IResult;
import com.pacewear.tws.phoneside.wallet.walletservice.IssueCard;
import com.qq.taf.jce.JceStruct;

import java.util.ArrayList;
import java.util.Iterator;

import org.json.JSONException;
import org.json.JSONObject;

import qrom.component.log.QRomLog;
import TRom.BusCardStatusInfo;
import TRom.E_PAY_ORDER_STATUS_APP;
import TRom.E_PAY_SCENE;
import TRom.GetCardStatusRsp;
import TRom.GetPayResultRsp;
import TRom.GetPayResultRspParam;
import TRom.OrderReqParam;
import TRom.OrderRspParam;
import TRom.UnifiedOrderRsp;

import android.text.TextUtils;

/**
 * @author baodingzhou
 */

public class Order implements IOrder, IOrderInner {

    private static final String TAG = "Order";

    private boolean mLocal = false;

    private int mBackendOrderStatus = E_PAY_ORDER_STATUS_APP._E_OSA_OPENCARD_FAIL;
    private OrderReqParam mOrderReqParam = null;
    private OrderRspParam mOrderRspParam = null;
    private GetPayResultRspParam mGetPayResultRspParam = null;

    private long mUniqueOrderCreatedID = -1;
    private int mRetryTimes = 0;
    private String mBusinessErrCode = "";

    public Order(BusCardStatusInfo busCardStatusInfo) {
        mLocal = false;
        mBackendOrderStatus = busCardStatusInfo.eOrderStatus;
        mOrderReqParam = busCardStatusInfo.stOrderParam;
        mOrderRspParam = busCardStatusInfo.stOrderRspParam;
        mGetPayResultRspParam = busCardStatusInfo.stPayResultParam;

        // TODO
        switch (mBackendOrderStatus) {
            case E_PAY_ORDER_STATUS_APP._E_OSA_OPENCARD_FAIL:
            case E_PAY_ORDER_STATUS_APP._E_OSA_CUT_FAIL_OPENCARD_FAIL:
                mCurrentOrderStep = mExecuteIssueStep;
                break;
            case E_PAY_ORDER_STATUS_APP._E_OSA_RECHARGE_FAIL:
            case E_PAY_ORDER_STATUS_APP._E_OSA_CUT_FAIL_TWSRECHARGE_FAIL:
            case E_PAY_ORDER_STATUS_APP._E_OSA_RECHARGE_DOUBT:
                mCurrentOrderStep = mExecuteTopupStep;
                break;
            case E_PAY_ORDER_STATUS_APP._E_OSA_SNOWORDER_FAIL:
            default:
                // TODO
                mCurrentOrderStep = mObtainBussinessOrderStep;
                break;
        }
    }

    public Order(OrderReqParam orderReqParam, long uniqueId) {
        mLocal = true;
        QRomLog.d(TAG, "new Order for uniqueSeq:" + uniqueId);
        QRomLog.d(TAG, "orderReqParam:" + JceStruct.toDisplaySimpleString(orderReqParam));
        mOrderReqParam = orderReqParam;
        mUniqueOrderCreatedID = uniqueId;
        setStep(mPlaceOrderStep);
    }

    private IStep<ORDER_STEP> mCurrentOrderStep = null;

    private final boolean setStep(IStep<ORDER_STEP> step) {

        boolean handle = false;

        if (step == null) {
            return handle;
        }

        if (mCurrentOrderStep != step) {
            IStep<ORDER_STEP> previousStep = mCurrentOrderStep;
            mCurrentOrderStep = step;
            if (previousStep != null) {
                previousStep.onQuitStep();
            }
            mCurrentOrderStep.onEnterStep();
            handle = true;
        }

        return handle;
    }

    private void repeatStep(){
        if(mCurrentOrderStep != null){
            mCurrentOrderStep.onQuitStep();
            mCurrentOrderStep.onEnterStep();
        }
    }

    private abstract class OrderStep extends Step<ORDER_STEP> {

        protected long mUniqueReq = -1;

        protected int mCurRetryTimes = 0;

        protected int mMaxRetryTimes = 0;

        public OrderStep(ORDER_STEP step,int maxTrytimes){
            this(step);
            mMaxRetryTimes = maxTrytimes;
        }

        public OrderStep(ORDER_STEP step) {
            super(step);
        }

        protected boolean canRetryBusiness() {
            if(mCurRetryTimes < mMaxRetryTimes){
                mCurRetryTimes++;
                return true;
            }
            return false;
        }

        protected void clearRetryTimes(){
            mCurRetryTimes = 0;
        }

        @Override
        protected boolean setStep(IStep<ORDER_STEP> step) {
            return Order.this.setStep(step);
        }

        @Override
        protected void notifyStepStatus(ORDER_STEP step, STATUS status) {

            // PLACE_ORDER需要告知tradeNo，不通过这种统一方式通知

            // TODO yuanqq
            if (step == ORDER_STEP.PLACE_ORDER) {
                return;
            }

            // TODO
            // 其它Step需确保mOrderRspParam非空且合法
            OrderManager.getInstanceInner()
                    .notifyOrderStatus(mOrderRspParam.sTradeNo, step, status);
        }
    }

    private final OrderStep mPlaceOrderStep = new OrderStep(ORDER_STEP.PLACE_ORDER, 4) {
        @Override
        public void onStepHandle() {
            if (mOrderReqParam == null) {
                // Notify new order created failed.
                // TODO 后续统一加入统一错误码，告知失败原因
                OrderManager.getInstanceInner().notifyNewOrder(mUniqueOrderCreatedID, false, null);
                return;
            }

            UnifiedOrder unifiedOrder = new UnifiedOrder();
            mUniqueReq = unifiedOrder.getUniqueSeq();
            unifiedOrder.setOrderReqParam(mOrderReqParam);
            boolean handled = unifiedOrder.invoke(new IResponseObserver() {
                @Override
                public void onResponseSucceed(long uniqueSeq, int operType, JceStruct response) {
                    if (mUniqueReq == uniqueSeq) {
                        UnifiedOrderRsp unifiedOrderRsp = (UnifiedOrderRsp) response;
                        boolean succeed = false;
                        if (unifiedOrderRsp != null && unifiedOrderRsp.iRet == 0
                                && unifiedOrderRsp.stOrderRspParam != null) {
                            // 是否需要更多校验？后台是否可靠？
                            mOrderRspParam = unifiedOrderRsp.stOrderRspParam;
                            succeed = true;
                        }

                        if (succeed) {
                            // Storce in OrderManager
                            OrderManager.getInstanceInner().addOrder(Order.this);
                            // Notify new order succeed
                            OrderManager.getInstanceInner().notifyNewOrder(mUniqueOrderCreatedID,
                                    succeed, mOrderRspParam.sTradeNo);
                            switchStep(mPayOrderStep);
                        } else {
                            // Notify new order created failed.
                            OrderManager.getInstanceInner().notifyNewOrder(mUniqueOrderCreatedID,
                                    succeed, null);
                        }
                        clearRetryTimes();
                    }
                }

                @Override
                public void onResponseFailed(long uniqueSeq, int operType, int errorCode,
                        String description) {
                    if (mUniqueReq == uniqueSeq) {
                        // Notify new order created failed.
                        if(errorCode == Constants.WALLET_ACCOUNT_AUTH_FAILED && canRetryBusiness()){
                            repeatStep();
                        } else {
                            OrderManager.getInstanceInner().notifyNewOrder(mUniqueOrderCreatedID,
                                false, null);
                            keepStep();
                            clearRetryTimes();
                        }
                    }
                }
            });

            if (!handled) {
                // Notify new order created failed.
                OrderManager.getInstanceInner().notifyNewOrder(mUniqueOrderCreatedID, false, null);
                keepStep();
            }
        }
    };

    private final OrderStep mPayOrderStep = new OrderStep(ORDER_STEP.PAY_ORDER) {
        @Override
        public void onStepHandle() {
            // TODO
            IPayManagerInner payManager = PayManager.getInstanceInner();
            if (payManager.pay(mOrderRspParam.sTradeNo)) {
                QRomLog.d(TAG, "paying");
            } else {
                QRomLog.d(TAG, "pay error");
                switchStep(mOrderPaidConfirmStep);
            }
        }
    };

    private final OrderStep mOrderPaidConfirmStep = new OrderStep(ORDER_STEP.ORDER_PAID_CONFIRM) {
        @Override
        public void onStepHandle() {
            GetPayResult getPayResult = new GetPayResult();
            mUniqueReq = getPayResult.getUniqueSeq();
            getPayResult.setPayOrderParams(mOrderReqParam.ePayType, mOrderRspParam.sTradeNo);
            boolean handled = getPayResult.invoke(new IResponseObserver() {
                @Override
                public void onResponseSucceed(long uniqueSeq, int operType, JceStruct response) {
                    if (mUniqueReq == uniqueSeq) {
                        boolean succeed = false;
                        boolean paySucceed = false;
                        GetPayResultRsp getPayResultRsp = (GetPayResultRsp) response;
                        if (getPayResultRsp.iRet == 0 && getPayResultRsp.stPayResultRspParam != null) {
                            mGetPayResultRspParam = getPayResultRsp.stPayResultRspParam;

                            // TODO
                            // 这里需要确认，支付失败是支付订单已结束进而本订单结束
                            if (mGetPayResultRspParam.iPayRet == 0) {
                                // 支付成功
                                paySucceed = true;
                            } else {
                                // 支付失败
                                paySucceed = false;
                            }

                            succeed = true;
                        }

                        if (succeed) {
                            if (paySucceed) {
                                // switchStep(mOrderPaidSucceedStep);
                                // } else {
                                // switchStep(mOrderPaidFailedStep);
                                // }
                                switchStep(mOrderPaidSucceedStep);
                            } else {
                                switchStep(mOrderPaidFailedStep);
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
    };

    private final OrderStep mOrderPaidFailedStep = new OrderStep(ORDER_STEP.ORDER_PAID_FAILED) {
        @Override
        public void onStepHandle() {
            // TODO 是否可以确保失败了订单必然关闭？
            // switchStep(mOrderFinishStep);
        }
    };

    private final OrderStep mOrderPaidSucceedStep = new OrderStep(ORDER_STEP.ORDER_PAID_SUCCEED) {
        @Override
        public void onStepHandle() {
            // 本状态仅起通知作用
            switchStep(mObtainBussinessOrderStep);
        }
    };

    private final OrderStep mObtainBussinessOrderStep = new OrderStep(
            ORDER_STEP.OBTAIN_BUSSINESS_ORDER) {
        @Override
        public void onStepHandle() {
            switch (mOrderReqParam.ePayScene) {
                case E_PAY_SCENE._EPS_OPEN_CARD:
                case E_PAY_SCENE._EPS_OPEN_CARD_ONLY:
                    switchStep(mExecuteIssueStep);
                    break;
                case E_PAY_SCENE._EPS_STAT:
                    switchStep(mExecuteTopupStep);
                    break;
                default:
                    keepStep();
                    break;
            }
        }
    };

    private final OrderStep mExecuteIssueStep = new OrderStep(ORDER_STEP.EXECUTE_ISSUE, 2) {
        @Override
        public void onStepHandle() {
            IssueCard issueCard = new IssueCard();
            mUniqueReq = issueCard.getSeqID();

            JsonObject params = new JsonObject();
            params.addProperty("instance_id", mOrderReqParam.stBusCardBaseInfo.sInstanceAId);
            params.addProperty("operation", "loadinstall");
            params.addProperty("token", mGetPayResultRspParam.sToken);
            final String aid = mOrderReqParam.getStBusCardBaseInfo().getSInstanceAId();
            final ICard card = CardManager.getInstance().getCard(aid);
            fillExtraInfoToParam(params, card);
            issueCard.putString(params.toString());
            QRomLog.e(TAG, "invoke issuecard cmd"+params.toString());
            boolean handled = issueCard.invoke(new IResult() {

                @Override
                public void onResult(long seqID, int ret, String[] outputParams,
                        Integer[] resultCode, byte[] bytes) {
                    if (mUniqueReq == seqID) {
                        boolean issueSucceed = false;
                        if (ret == 0) {
                            issueSucceed = true;
                            // 开卡成功，此处需改变install状态
                            ((ICardInner) card).setInstallStatus(INSTALL_STATUS.PERSONAL);
                            mBusinessErrCode = "";
                        } else {
                            mBusinessErrCode = parseErrorCode(ret, outputParams);
                        }
                        if (issueSucceed || canIgnoreError(aid, true)) {
                            // 当前设计，开卡必然包含充值
                            if (mOrderReqParam.ePayScene == E_PAY_SCENE._EPS_OPEN_CARD_ONLY) {
                                switchStep(mOrderFinishStep);
                            } else {
                                switchStep(mExecuteTopupStep);
                            }
                            clearRetryTimes();
                        } else {
                             if(canRetryBusiness()){
                                 repeatStep();
                             } else {
                                 keepStep();
                                 clearRetryTimes();
                             }
                        }
                    }
                }

                @Override
                public void onExecption(long seqID, int error) {
                    if (mUniqueReq == seqID) {
                        keepStep();
                        clearRetryTimes();
                    }
                }
            });

            if (!handled) {
                keepStep();
            }
        }
    };

    private final OrderStep mExecuteTopupStep = new OrderStep(ORDER_STEP.EXECUTE_TOPUP, 2) {
        @Override
        public void onStepHandle() {

            CardTopUp cardTopup = new CardTopUp();
            mUniqueReq = cardTopup.getSeqID();

            JsonObject params = new JsonObject();
            params.addProperty("instance_id", mOrderReqParam.stBusCardBaseInfo.sInstanceAId);
            params.addProperty("token", mGetPayResultRspParam.sToken);
            final String aid = mOrderReqParam.getStBusCardBaseInfo().getSInstanceAId();
            final ICard card = CardManager.getInstance().getCard(aid);
            fillExtraInfoToParam(params, card);
            cardTopup.putString(params.toString());
            QRomLog.e(TAG, "invoke topup cmd:"+params.toString());
            boolean handled = cardTopup.invoke(new IResult() {

                @Override
                public void onResult(long seqID, int ret, String[] outputParams,
                        Integer[] resultCode, byte[] bytes) {
                    if (mUniqueReq == seqID) {
                        boolean topupSucceed = false;
                        if (ret == 0) {
                            topupSucceed = true;
                            mBusinessErrCode = "";
                        } else {
                            mBusinessErrCode = parseErrorCode(ret, outputParams);
                        }
                        // TODO 
                        if (topupSucceed || canIgnoreError(aid, false)) {
                            switchStep(mOrderFinishStep);
                            clearRetryTimes();
                        } else {
                            if(canRetryBusiness()){
                                repeatStep();
                            } else {
                                keepStep();
                                clearRetryTimes();
                            }
                        }
                    }
                }

                @Override
                public void onExecption(long seqID, int error) {
                    if (mUniqueReq == seqID) {
                        keepStep();
                        clearRetryTimes();
                    }
                }
            });

            if (!handled) {
                keepStep();
            }
        }
    };

    private final OrderStep mOrderFinalConfirmStep = new OrderStep(
            ORDER_STEP.ORDER_FINAL_CONFIRM, 4) {
        @Override
        public void onStepHandle() {
            LatestCardStatus cardStatus = new LatestCardStatus();
            mUniqueReq = cardStatus.getUniqueSeq();
            boolean handled = cardStatus.invoke(new IResponseObserver() {

                @Override
                public void onResponseSucceed(long uniqueSeq, int operType, JceStruct response) {
                    if (mUniqueReq == uniqueSeq) {
                        // TODO
                        // 当前缺少查询单一订单状态的接口，这里通过遍历未完成订单来判断
                        GetCardStatusRsp rsp = (GetCardStatusRsp) response;
                        boolean orderFinish = false;
                        if (rsp.iRet == 0) {
                            orderFinish = true;
                            ArrayList<BusCardStatusInfo> cardStatusInfos = rsp.getVStatusList();
                            if (cardStatusInfos != null) {
                                Iterator<BusCardStatusInfo> iterator = cardStatusInfos.iterator();
                                BusCardStatusInfo cardStatusInfo = null;
                                while (iterator.hasNext()) {
                                    cardStatusInfo = iterator.next();
                                    if (mOrderRspParam.sTradeNo
                                            .equalsIgnoreCase(
                                                    cardStatusInfo.stOrderRspParam.sTradeNo)) {
                                        orderFinish = false;
                                    }
                                }
                            }
                        }

                        if (orderFinish) {
                            switchStep(mOrderFinishStep);
                            clearRetryTimes();
                        } else {
                            // TODO 存疑的情况，或许需要人工服务、退款
                            if(canRetryBusiness()){
                                repeatStep();
                            } else {
                                keepStep();
                                clearRetryTimes();
                            }
                        }
                    }
                }

                @Override
                public void onResponseFailed(long uniqueSeq, int operType, int errorCode,
                        String description) {
                    if (mUniqueReq == uniqueSeq) {
                        keepStep();
                        clearRetryTimes();
                    }
                }
            });

            if (!handled) {
                keepStep();
            }
        }
    };

    private final OrderStep mOrderFinishStep = new OrderStep(ORDER_STEP.ORDER_FINISH) {
        @Override
        public void onStepHandle() {
            // do nothing
        }
    };

    private String parseErrorCode(int ret, String[] outputParams) {
        String output = (outputParams != null && outputParams.length > 0) ? outputParams[0] : null;
        JSONObject root = null;
        String sp_code = "";
        StringBuilder ret_sp = new StringBuilder(Constants.WALLET_ERR_PLATFORM_BEIJING).append("-");
        String ret_snowball = new StringBuilder(Constants.WALLET_ERR_PLATFORM_SNOWBALL)
                .append("-").append(ret).toString();
        if (TextUtils.isEmpty(output)) {
            return ret_snowball;
        }
        try {
            root = new JSONObject(output);
        } catch (JSONException e) {
            QRomLog.e(TAG, e.getMessage());
            return ret_snowball;
        }
        sp_code = root.optString("code");
        ret_sp = ret_sp.append(sp_code);
        return (!TextUtils.isEmpty(sp_code)) ? ret_sp.toString() : ret_snowball;
    }

    @Override
    public OrderRspParam getOrderRspParam() {
        return mOrderRspParam;
    }

    @Override
    public OrderReqParam getOrderReqParam() {
        return mOrderReqParam;
    }

    @Override
    public GetPayResultRspParam getGetPayResultRspParam() {
        return mGetPayResultRspParam;
    }

    @Override
    public void setLocalPaid(boolean paid) {
        mPayOrderStep.setStep(paid ? mOrderPaidConfirmStep : mOrderPaidFailedStep);
    }

    @Override
    public boolean isLocal() {
        return mLocal;
    }

    @Override
    public boolean isIssueFail() {
        if (mOrderReqParam == null || (mOrderReqParam.ePayScene != E_PAY_SCENE._EPS_OPEN_CARD
                && mOrderReqParam.ePayScene != E_PAY_SCENE._EPS_OPEN_CARD_ONLY)) {
            return false;
        }
        if (isInValidOrder()) {
            return false;
        }
        return !mOrderFinishStep.isCurrentStep();
    }

    @Override
    public boolean isCardTopFail() {
        if (mOrderReqParam == null || mOrderReqParam.ePayScene != E_PAY_SCENE._EPS_STAT) {
            return false;
        }
        if (isInValidOrder()) {
            return false;
        }
        return !mOrderFinishStep.isCurrentStep();
    }

    @Override
    public int getRetryTime() {
        return mRetryTimes;
    }

    @Override
    public int onRetry() {
        if (isInValidOrder() || mOrderFinishStep.isCurrentStep()) {
            return -1;
        }
        if (!mCurrentOrderStep.isCurrentStep()) {
            // 此处处理第一次进来时，需处理服务器未完成订单，所以要先将mStatus(初始值为QUIT)置为ENTER
            mCurrentOrderStep.onEnterStep();
        } else {
            mCurrentOrderStep.onStep();
        }
        mRetryTimes++;
        return 0;
    }

    @Override
    public ORDER_STEP getOrderStep() {
        return mCurrentOrderStep.getStep();
    }

    @Override
    public boolean isInValidOrder() {
        return mOrderPaidFailedStep.isCurrentStep();
    }

    @Override
    public boolean isIdle() {
        STATUS curStatus = mCurrentOrderStep.getStatus();
        ORDER_STEP curStep = mCurrentOrderStep.getStep();
        if (curStep == ORDER_STEP.ORDER_FINISH
                || curStep == ORDER_STEP.ORDER_PAID_FAILED) {
            return true;
        }
        return (curStatus == STATUS.KEEP || curStatus == STATUS.QUIT);
    }

    @Override
    public String getBusinessErr() {
        return mBusinessErrCode;
    }

    private void fillExtraInfoToParam(JsonObject param, ICard card) {
        String extrainfo = card.getExtra_Info();
        String phoneNum = EnvManager.getInstanceInner().getUserPhoneNum();
        if (!TextUtils.isEmpty(phoneNum)) {
            card.setExtra_Info("mobnum", phoneNum);
            extrainfo = card.getExtra_Info();
        }
        if (CONFIG.LINGNANTONG.mAID.equalsIgnoreCase(card.getAID())) {
            card.setExtra_Info("city_code", Utils.getUserCacheCityCode());
            extrainfo = card.getExtra_Info();
        }
        if (TextUtils.isEmpty(extrainfo)) {
            return;
        }
        param.addProperty("extra_info", extrainfo);
    }

    private boolean canIgnoreError(String aid, boolean isIssueCard) {
        boolean canIgnore = false;
            if (isIssueCard) {
                canIgnore = mBusinessErrCode.contains(Constants.WALLET_BEIJING_DUPLITE_OPENCARD)
                        || mBusinessErrCode.contains(Constants.WALLET_BEIJING_DUPLITE_PERSONAL);
            } else {
                canIgnore = mBusinessErrCode.contains(Constants.WALLET_BEIJING_DUPLITE_TOPUP);
        }
        return canIgnore;
    }
}
