
package com.pacewear.tws.phoneside.wallet.ui2.widget;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.pacewear.tws.phoneside.wallet.R;
import com.pacewear.tws.phoneside.wallet.bean.OrderBean;
import com.pacewear.tws.phoneside.wallet.card.CardManager;
import com.pacewear.tws.phoneside.wallet.card.ICard;
import com.pacewear.tws.phoneside.wallet.card.ICardManager;
import com.pacewear.tws.phoneside.wallet.card.ICard.ACTIVATION_STATUS;
import com.pacewear.tws.phoneside.wallet.order.IOrder;
import com.pacewear.tws.phoneside.wallet.order.OrderManager;
import com.pacewear.tws.phoneside.wallet.ui2.activity.BusinessLoadingActivity;
import com.pacewear.tws.phoneside.wallet.ui2.activity.SetDefaultActivity;
import com.pacewear.tws.phoneside.wallet.ui2.activity.TrafficCardDetailActivity;
import com.tencent.tws.pay.PayNFCConstants;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseCardView extends FrameLayout {
    public static final int SENCE_LIST = 0;
    public static final int SENCE_SINGLE = 1;
    public static final int SENCE_ONLYBG = 2;
    protected final Context mContext;
    private ImageView mWalletCard = null;
    private RelativeLayout mNetFaceShade = null;
    private RelativeLayout mLocalFaceShade = null;
    private TextView mLocalShadeText = null;
    private TextView mNetShadeText = null;
    private ProgressBar mLoadingSpinner = null;
    private Button mDefaultTag = null;
    protected ICard mCardAttached = null;
    private IOrder mOrder = null;
    // private ICardModulePresent mCardModulePresent = null;
    private int mDisplayScene = SENCE_LIST;
    private List<BaseViewHandler> mBaseViewHandlers = new ArrayList<BaseViewHandler>();

    public BaseCardView(Context context) {
        this(context, null);
    }

    public BaseCardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }

    protected final void init() {
        Resources resource = getResources();
        setPadding(
                resource.getDimensionPixelSize(R.dimen.base_card_padding_left),
                resource.getDimensionPixelSize(R.dimen.base_card_padding_top),
                resource.getDimensionPixelSize(R.dimen.base_card_padding_right),
                resource.getDimensionPixelSize(R.dimen.base_card_padding_bottom));
        LayoutInflater.from(mContext).inflate(getFlatLayout(), this);
        initView();
        onPostInit();
        mBaseViewHandlers.add(mCardLoading);
        mBaseViewHandlers.add(mCardRefunding);
        mBaseViewHandlers.add(mCardIssueFail);
        mBaseViewHandlers.add(mCardTopupFail);
        mBaseViewHandlers.add(mCardNormal);
        mBaseViewHandlers.add(mCardInfoSyncFail);
    }

    protected void initView() {
        mWalletCard = (ImageView) findViewById(R.id.wallet_card);
        mDefaultTag = (Button) findViewById(R.id.default_tag);
        mLocalFaceShade = (RelativeLayout) findViewById(R.id.face_shade);
        mNetFaceShade = (RelativeLayout) findViewById(R.id.card_fail_lay);
        mLocalShadeText = (TextView) findViewById(R.id.face_shade_text);
        mNetShadeText = (TextView) findViewById(R.id.card_fail_tip);
        mLoadingSpinner = (ProgressBar) findViewById(R.id.face_shade_spinner);
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
            return mOrder != null && mOrder.isIssueFail() && mDisplayScene != SENCE_ONLYBG;
        }

        @Override
        public void onHandle() {
            showNetShadeText(R.string.activate_card_continue);
            changeCardBackgroud(false);
            setOnClickEvent(jumpContinueIssue);
        }

    };

    // 充值失败显示界面
    private BaseViewHandler mCardTopupFail = new BaseViewHandler() {

        @Override
        public void onHandle() {
            showNetShadeText(R.string.charge_card_continue);
            setOnClickEvent(jumpDetailPage);
        }

        @Override
        public boolean isConditionReady() {
            return mOrder != null && mOrder.isCardTopFail() && mDisplayScene != SENCE_ONLYBG;
        }
    };

    private BaseViewHandler mCardInfoSyncFail = new BaseViewHandler() {

        @Override
        public void onHandle() {
            showLocalShadeText(R.string.wallet_sync_err_watch);
            setOnClickEvent(null);
        }

        @Override
        public boolean isConditionReady() {
            ICardManager cardManager = CardManager.getInstance();
            return (!cardManager.isReady() && !cardManager.isInSyncProcess())
                    && mDisplayScene != SENCE_ONLYBG;
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
            return mDisplayScene != SENCE_ONLYBG;
        }
    };

    // 一直加载，转菊花
    private BaseViewHandler mCardLoading = new BaseViewHandler() {

        @Override
        public void onHandle() {
            showLoading();
            setOnClickEvent(null);
        }

        @Override
        public boolean isConditionReady() {
            return (CardManager.getInstance().isInSyncProcess()
                    || OrderManager.getInstance().isInOrderSyncProcess())
                    && mDisplayScene != SENCE_ONLYBG;
        }
    };
    // 退款中
    private BaseViewHandler mCardRefunding = new BaseViewHandler() {

        @Override
        public void onHandle() {
            showNetShadeText(R.string.wallet_cardlist_refunding);
            setOnClickEvent(jumpDetailPage);
        }

        @Override
        public boolean isConditionReady() {
            return /* mDisplayScene != SENCE_ONLYBG */ false;
        }
    };

    private BaseViewHandler mCardNormal = new BaseViewHandler() {

        @Override
        public void onHandle() {
            showNormal();
            setOnClickEvent(jumpDetailPage);
        }

        @Override
        public boolean isConditionReady() {
            return mCardAttached.isReady() || (mDisplayScene == SENCE_ONLYBG);
        }
    };
    private OnClickListener jumpDetailPage = new OnClickListener() {

        @Override
        public void onClick(View v) {
            Intent intent = new Intent(mContext, TrafficCardDetailActivity.class);
            intent.putExtra(PayNFCConstants.ExtraKeyName.EXTRA_STR_INSTANCE_ID,
                    mCardAttached.getAID());
            mContext.startActivity(intent);
        }
    };
    private OnClickListener jumpContinueIssue = new OnClickListener() {

        @Override
        public void onClick(View v) {
            Intent intent = new Intent(mContext, BusinessLoadingActivity.class);
            intent.putExtra(BusinessLoadingActivity.KEY_ORDER_BEAN,
                    OrderBean.genByLastOrder(mCardAttached.getAID(), mOrder));
            mContext.startActivity(intent);
        }
    };

    public final void attachCard(ICard card, int scene) {
        mCardAttached = card;
        mDisplayScene = scene;
        changeCardBackgroud(true);
        showDefaultCardTag();
        mOrder = OrderManager.getInstance().getLastOrder(card.getAID());
        invokeCardViewHandler();
        onUpdate(scene);
    }

    private void invokeCardViewHandler() {
        for (BaseViewHandler viewHandler : mBaseViewHandlers) {
            if (viewHandler.isConditionReady()) {
                viewHandler.onHandle();
                break;
            }
        }
    }

    private void showNetShadeText(int resId) {
        mNetFaceShade.setVisibility(View.VISIBLE);
        mLocalFaceShade.setVisibility(View.GONE);
        mNetShadeText.setText(resId);
    }

    private void showLocalShadeText(int resId) {
        mNetFaceShade.setVisibility(View.GONE);
        mLocalFaceShade.setVisibility(View.VISIBLE);
        mLocalShadeText.setText(resId);
        mLoadingSpinner.setVisibility(View.GONE);
    }

    private void showLoading() {
        mNetFaceShade.setVisibility(View.GONE);
        mLocalFaceShade.setVisibility(View.VISIBLE);
        mLocalShadeText.setText("");
        mLoadingSpinner.setVisibility(View.VISIBLE);
    }

    private void showNormal() {
        mNetFaceShade.setVisibility(View.GONE);
        mLocalFaceShade.setVisibility(View.GONE);
        mLocalShadeText.setText("");
        mLoadingSpinner.setVisibility(View.GONE);
    }

    private void showDefaultCardTag() {
        boolean isDefault = mCardAttached.getActivationStatus() == ACTIVATION_STATUS.ACTIVATED;
        ICard[] cardList = CardManager.getInstance().getCard();
        int totalSize = (cardList != null) ? cardList.length : 0;
        mDefaultTag.setVisibility(
                (mDisplayScene == SENCE_LIST && isDefault && totalSize > 1) ? View.VISIBLE
                        : View.GONE);
    }

    private void changeCardBackgroud(boolean isNormal) {
        mWalletCard.setImageResource(
                isNormal ? mCardAttached.getCardBg() : mCardAttached.getCardDisableBg());
    }

    private void setOnClickEvent(OnClickListener event) {
        if (event == null || mDisplayScene == SENCE_SINGLE) {
            this.setEnabled(false);
        } else {
            this.setEnabled(true);
            this.setOnClickListener(event);
        }
    }

    protected abstract int getFlatLayout();

    protected abstract void onPostInit();

    protected abstract void onUpdate(int scene);

    interface BaseViewHandler {
        public boolean isConditionReady();

        public void onHandle();
    }
}
