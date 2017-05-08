
package com.pacewear.tws.phoneside.wallet.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.pacewear.tws.phoneside.wallet.R;
import com.pacewear.tws.phoneside.wallet.card.ICard;
import com.tencent.tws.pay.PayNFCConstants;

public class SimpleCardListItem extends FrameLayout {

    protected static final String TAG = SimpleCardListItem.class.getSimpleName();

    protected final Context mContext;

    protected int mType = PayNFCConstants.Card.TYPE_UNKNOWN;

    private ImageView mItemIcon = null;

    private TextView mItemText = null;

    private ImageView mItemSelected = null;

    private ProgressBar mItemLoading = null;

    protected ICard mCardAttached = null;

    public SimpleCardListItem(Context context) {
        this(context, null);
    }

    public SimpleCardListItem(Context context, AttributeSet attrs) {
        super(context, attrs);

        mContext = context;

        LayoutInflater.from(context).inflate(R.layout.wallet_simple_card_list_item, this);

        initView();
    }

    protected void initView() {
        mItemIcon = (ImageView) findViewById(R.id.simple_list_item_icon);
        mItemText = (TextView) findViewById(R.id.simple_list_item_text);
        mItemSelected = (ImageView) findViewById(R.id.simple_list_item_selected);
        mItemLoading = (ProgressBar) findViewById(R.id.simple_list_item_loading);
    }

    public final void attachCard(ICard card) {
        mCardAttached = card;
        mItemIcon.setImageResource(mCardAttached.getCardICon());
        mItemText.setText(mCardAttached.getCardName());
    }

    public final ICard getAttachCard() {
        return mCardAttached;
    }

    public void setIcon(int resid) {
        mItemIcon.setImageResource(resid);
    }

    public void setDescription(int resid) {
        mItemText.setText(resid);
    }

    public void setDescription(String strVal) {
        mItemText.setText(strVal);
    }

    public void setRightBitmap(int resId) {
        if (resId != 0) {
            mItemSelected.setImageResource(resId);
            mItemSelected.setVisibility(VISIBLE);
            mItemLoading.setVisibility(GONE);
        } else {
            mItemSelected.setVisibility(GONE);
            mItemLoading.setVisibility(GONE);
        }
    }

    public final boolean setItemSelect(boolean selected) {
        if (selected) {
            mItemSelected.setVisibility(VISIBLE);
            mItemLoading.setVisibility(GONE);
        } else {
            mItemSelected.setVisibility(View.GONE);
        }

        return true;
    }

    public void showItemLoading() {
        mItemLoading.setVisibility(VISIBLE);
        mItemSelected.setVisibility(GONE);
    }

    public void hideItemLoading() {
        mItemLoading.setVisibility(GONE);
    }
}
