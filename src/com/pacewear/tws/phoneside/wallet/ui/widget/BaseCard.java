
package com.pacewear.tws.phoneside.wallet.ui.widget;

import android.content.Context;
import android.content.res.Resources;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.tencent.tws.gdevicemanager.R;
import com.tencent.tws.pay.PayNFCConstants;
import com.tencent.tws.phoneside.walletv2.card.CardManager;
import com.tencent.tws.phoneside.walletv2.card.ICard;
import com.tencent.tws.phoneside.walletv2.card.ICard.ACTIVATION_STATUS;
import com.tencent.tws.phoneside.walletv2.card.ICardManager;
import com.tencent.tws.phoneside.walletv2.order.IOrder;
import com.tencent.tws.phoneside.walletv2.order.IOrderManager;
import com.tencent.tws.phoneside.walletv2.order.OrderManager;
import com.tencent.tws.phoneside.walletv2.ui.fragments.CardsFragment;

public abstract class BaseCard extends FrameLayout {

    protected static final String TAG = BaseCard.class.getSimpleName();

    protected final Context mContext;

    protected int mType = PayNFCConstants.Card.TYPE_UNKNOWN;

    protected CardsFragment mCardsFragment = null;

    protected View mWalletCard = null;

    protected ImageView mDefaultTag = null;

    protected ICard mCardAttached = null;

    protected RelativeLayout mFaceShade = null;
    protected TextView mShadeText = null;
    protected ProgressBar mLoadingSpinner = null;

    protected boolean mHideWhenShowDetail = false;

    public BaseCard(Context context) {
        this(context, null);
    }

    public BaseCard(Context context, AttributeSet attrs) {
        super(context, attrs);

        mContext = context;

        Resources resource = getResources();

        setPadding(
                resource.getDimensionPixelSize(R.dimen.base_card_padding_left),
                resource.getDimensionPixelSize(R.dimen.base_card_padding_top),
                resource.getDimensionPixelSize(R.dimen.base_card_padding_right),
                resource.getDimensionPixelSize(R.dimen.base_card_padding_bottom));
    }

    protected abstract void inflate(Context context);

    protected void initView() {
        mWalletCard = findViewById(R.id.wallet_card);
        mDefaultTag = (ImageView) findViewById(R.id.default_tag);
        mFaceShade = (RelativeLayout) findViewById(R.id.face_shade);
        mShadeText = (TextView) findViewById(R.id.face_shade_text);
        mLoadingSpinner = (ProgressBar) findViewById(R.id.face_shade_spinner);
    }

    public final void setCardsFragment(CardsFragment cardFragment) {
        mCardsFragment = cardFragment;
    }

    protected abstract boolean isShowDefualtTag();

    public final void attachCard(ICard card) {
        mCardAttached = card;

        if (mCardAttached != null) {
            mDefaultTag.setVisibility(isShowDefualtTag()
                    && mCardAttached.getActivationStatus() == ACTIVATION_STATUS.ACTIVATED
                            ? View.VISIBLE : View.GONE);
            onCardAttached();
        }
    }

    public final void reAttach() {
        attachCard(mCardAttached);
    }

    protected void onCardAttached() {
        mWalletCard.setBackgroundResource(mCardAttached.getCardBg());
        IOrderManager orderManager = OrderManager.getInstance();
        ICardManager cardManager = CardManager.getInstance();
        IOrder order = orderManager.getLastOrder(mCardAttached.getAID());
        String cardinfoErrDesc = mCardAttached.getCardInfoErrDesc();
        if (!cardManager.isAvaliable()) {
            showLoadingSpinner();
        } else if (!cardManager.isReady()) {
            if (!cardManager.isInSyncProcess()) {
                showShadeText(R.string.wallet_sync_err_watch);
            } else {
                showLoadingSpinner();
            }
        } else if (!orderManager.isOrderReady()) {
            if (orderManager.isInOrderSyncProcess()) {
                showLoadingSpinner();
            } else {
                showShadeText(R.string.wallet_sync_err_network);
            }
        } else if (order != null && order.isIssueFail()) {
            showShadeText(R.string.activate_card_continue);
        } else if (order != null && order.isCardTopFail()) {
            showShadeText(R.string.charge_card_continue);
        } else if (!TextUtils.isEmpty(cardinfoErrDesc)) {
            showShadeText(R.string.wallet_card_invalid);
        } else if (!mCardAttached.isAvaliable()) {
            showLoadingSpinner();
        } else {
            hideFaceShade();
        }
        if (!TextUtils.isEmpty(cardinfoErrDesc)) {
            this.setEnabled(true);
        } else if (!CardManager.getInstance().isReady()
                || !OrderManager.getInstance().isOrderReady()) {
            this.setEnabled(false);
        } else {
            this.setEnabled(true);
        }
    };

    public void showLoadingSpinner() {
        if (!mHideWhenShowDetail) {
            mFaceShade.setVisibility(VISIBLE);
            mShadeText.setVisibility(GONE);
            mLoadingSpinner.setVisibility(VISIBLE);
        }
    }

    public void showShadeText(int resId) {
        mShadeText.setText(resId);
        if (!mHideWhenShowDetail) {
            mFaceShade.setVisibility(VISIBLE);
            mShadeText.setVisibility(VISIBLE);
            mLoadingSpinner.setVisibility(GONE);
        }
    }

    public void showShadeText(String resDesc) {
        mShadeText.setText(resDesc);
        if (!mHideWhenShowDetail) {
            mFaceShade.setVisibility(VISIBLE);
            mShadeText.setVisibility(VISIBLE);
            mLoadingSpinner.setVisibility(GONE);
        }
    }

    public void hideFaceShade() {
        mFaceShade.setVisibility(GONE);
    }

    public void hideWhenShowDetail(boolean hide) {
        mHideWhenShowDetail = hide;
        if (mHideWhenShowDetail) {
            hideFaceShade();
        }
    }
}
