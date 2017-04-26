
package com.pacewear.tws.phoneside.wallet.ui2.activity;

import android.app.TwsActivity;
import android.widget.TextView;

import com.pacewear.tws.phoneside.wallet.R;
import com.pacewear.tws.phoneside.wallet.WalletApp;
import com.pacewear.tws.phoneside.wallet.order.IOrder;
import com.pacewear.tws.phoneside.wallet.ui2.widget.BaseViewChain;
import com.pacewear.tws.phoneside.wallet.ui2.widget.BaseViewChain.BaseViewHandler;
import com.tencent.tws.assistant.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class BusinessLoadingActivity extends TwsActivity {
    private IOrder mOrder = null;
    private BaseViewChain mBaseViewChain = null;
    private boolean mIsTopupInvoke = false;
    private final int RESULT_UNKOWN = 9999;
    private int mExeResult = RESULT_UNKOWN;
    private String mNextTitle = null;
    // chain mode
    private BaseViewHandler mOrderIsNull = new BaseViewHandler() {

        @Override
        public void onHandle() {
            finishAndToast(R.string.wallet_no_order);
        }

        @Override
        public boolean isConditionReady() {
            return mOrder == null;
        }
    };
    private BaseViewHandler mOrderIsInvalid = new BaseViewHandler() {

        @Override
        public void onHandle() {
            finishAndToast(R.string.wallet_invalid_order);
        }

        @Override
        public boolean isConditionReady() {
            return mOrder.isInValidOrder();
        }
    };
    private BaseViewHandler mOrderIsSuc = new BaseViewHandler() {

        @Override
        public void onHandle() {
        }

        @Override
        public boolean isConditionReady() {
            return mExeResult == 0;
        }
    };

    private void showTitle(int resTitle) {

    }

    private void showDesc(int resDesc) {

    }

    private void finishAndToast(int strRes) {
        Toast.makeText(WalletApp.getHostAppContext(), getString(strRes), Toast.LENGTH_LONG)
                .show();
        finish();
    }

    ///////////////////// Inner Class////////////////////
    class ResultFilterChain {
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

        final Result invoke(int result, IOrder order) {
            if (mList.size() <= 0) {
                return null;
            }
            return mList.get(0).filter(result, order);
        }
    }

    abstract class ResultFilter {
        private Result mResult = null;
        private ResultFilter mNextFilter = null;

        public ResultFilter(int finish, int resTitle) {
            mResult = new Result(finish, resTitle);
        }

        public void setNext(ResultFilter next) {
            mNextFilter = next;
        }

        public final Result filter(int result, IOrder order) {
            if (onFilter(result, order)) {
                return mResult;
            }
            return mNextFilter != null ? mNextFilter.filter(result, order) : null;
        }

        abstract boolean onFilter(int result, IOrder order);
    }

    class Result {
        private int iFinish;
        private int iTitleRes;

        Result(int finsh, int res) {
            iFinish = finsh;
            iTitleRes = res;
        }
    }
}
