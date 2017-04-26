
package com.pacewear.tws.phoneside.wallet.ui2.widget;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.pacewear.tws.phoneside.wallet.R;
import com.pacewear.tws.phoneside.wallet.card.CardManager;
import com.pacewear.tws.phoneside.wallet.card.ICard;
import com.pacewear.tws.phoneside.wallet.card.ICard.ACTIVATION_STATUS;
import com.pacewear.tws.phoneside.wallet.order.Order;
import com.pacewear.tws.phoneside.wallet.order.OrderManager;
import com.pacewear.tws.phoneside.wallet.present.ICardModulePresent;
import com.pacewear.tws.phoneside.wallet.ui2.activity.SetDefaultActivity;
import com.pacewear.tws.phoneside.wallet.ui2.widget.BaseViewChain.BaseViewHandler;

import java.util.List;

public abstract class BaseCardView extends FrameLayout {
    protected final Context mContext;
    protected View mWalletCard = null;
    protected RelativeLayout mFaceShade = null;
    protected TextView mShadeText = null;
    protected ProgressBar mLoadingSpinner = null;
    protected Button mDefaultTag = null;
    private ICard mCard = null;
    private Order mOrder = null;
    private ICardModulePresent mCardModulePresent = null;
    private boolean showInDetailPage = false;
    private BaseViewChain mCardViewChain = null;

    public BaseCardView(Context context) {
        this(context, null);
    }

    public BaseCardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;

        Resources resource = getResources();
        setPadding(
                resource.getDimensionPixelSize(R.dimen.base_card_padding_left),
                resource.getDimensionPixelSize(R.dimen.base_card_padding_top),
                resource.getDimensionPixelSize(R.dimen.base_card_padding_right),
                resource.getDimensionPixelSize(R.dimen.base_card_padding_bottom));
        initView();
        init();
    }

    private void init() {
        // TODO NEXT
        mCardViewChain = new BaseViewChain();
        mCardViewChain.add(mCardLoading);
        mCardViewChain.add(mCardInfoSyncFail);
        mCardViewChain.add(mCardRefunding);
        mCardViewChain.add(mCardIssueFail);
        mCardViewChain.add(mCardTopupFail);
        mCardViewChain.add(mCardNormal);
    }

    protected abstract void onInflate(Context context);

    protected void initView() {
        mWalletCard = findViewById(R.id.wallet_card);
        mDefaultTag = (Button) findViewById(R.id.default_tag);
        mFaceShade = (RelativeLayout) findViewById(R.id.face_shade);
        mShadeText = (TextView) findViewById(R.id.face_shade_text);
        mLoadingSpinner = (ProgressBar) findViewById(R.id.face_shade_spinner);
        showDefaultCardTag();
        mDefaultTag.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent it = new Intent(mContext, SetDefaultActivity.class);
                mContext.startActivity(it);
            }
        });
    }

    // 开卡失败显示界面
    private BaseViewHandler mCardIssueFail = new BaseViewHandler() {

        @Override
        public boolean isConditionReady() {
            return mOrder != null && mOrder.isIssueFail();
        }

        @Override
        public void onHandle() {
            showShadeText(R.string.activate_card_continue);
        }

    };

    // 充值失败显示界面
    private BaseViewHandler mCardTopupFail = new BaseViewHandler() {

        @Override
        public void onHandle() {
            showShadeText(R.string.charge_card_continue);
        }

        @Override
        public boolean isConditionReady() {
            return mOrder != null && mOrder.isCardTopFail();
        }
    };

    private BaseViewHandler mCardInfoSyncFail = new BaseViewHandler() {

        @Override
        public void onHandle() {
            showShadeText(R.string.wallet_sync_err_watch);
        }

        @Override
        public boolean isConditionReady() {
            return !mCard.isReady();
        }
    };

    private BaseViewHandler mNetError = new BaseViewHandler() {

        @Override
        public void onHandle() {
            // TODO Auto-generated method stub

        }

        @Override
        public boolean isConditionReady() {
            // TODO Auto-generated method stub
            return false;
        }
    };

    // 一直加载，转菊花
    private BaseViewHandler mCardLoading = new BaseViewHandler() {

        @Override
        public void onHandle() {
            showLoading();
        }

        @Override
        public boolean isConditionReady() {
            return CardManager.getInstance().isInSyncProcess()
                    || OrderManager.getInstance().isInOrderSyncProcess();
        }
    };

    private BaseViewHandler mCardRefunding = new BaseViewHandler() {

        @Override
        public void onHandle() {

        }

        @Override
        public boolean isConditionReady() {
            return false;
        }
    };

    private BaseViewHandler mCardNormal = new BaseViewHandler() {

        @Override
        public void onHandle() {
            // TODO Auto-generated method stub

        }

        @Override
        public boolean isConditionReady() {
            return true;
        }
    };

    public void onUpdate() {
        showDefaultCardTag();
        mCardViewChain.invoke();
    }

    private void showShadeText(int resId) {

    }

    private void showLoading() {
    }

    private void showDefaultCardTag() {
        boolean isDefault = mCard.getActivationStatus() == ACTIVATION_STATUS.ACTIVATED;
        List<ICard> cardList = mCardModulePresent.getCardList();
        int totalSize = (cardList != null) ? cardList.size() : 0;
        mDefaultTag.setVisibility(
                (!showInDetailPage && isDefault && totalSize > 1) ? View.VISIBLE : View.GONE);
    }
}
