
package com.pacewear.tsm.business;

import android.text.TextUtils;

import com.pacewear.tsm.ITsmBusinessListener;
import com.pacewear.tsm.card.TsmCard;
import com.pacewear.tsm.card.TsmContext;
import com.pacewear.tsm.internal.TsmListState;

import TRom.E_TSM_CARD_ELEMENT;

public class TsmCardListQuery extends TsmBaseBusiness {

    public TsmCardListQuery(TsmContext ctx) {
        super(ctx);
    }

    @Override
    protected boolean onStart() {
        String cache = getQueryResult();
        if (!TextUtils.isEmpty(cache)) {
            ITsmBusinessListener callback = mContext.getITsmBusinessListener();
            if (callback != null) {
                callback.onSuccess(cache);
                return false;
            }
        }
        String mainAID = mContext.getCard().getMainAID();
        addProcess(new TsmListState(mContext, mainAID,
                E_TSM_CARD_ELEMENT._ETSME_SECRITE_DOMAIN));
        addProcess(new TsmListState(mContext, mainAID,
                E_TSM_CARD_ELEMENT._ETSME_APP));
        addProcess(new TsmListState(mContext, mainAID,
                E_TSM_CARD_ELEMENT._ETSME_LOAD_FILES));
        return true;
    }

    private String getQueryResult() {
        TsmCard card = mContext.getCard();
        return card.getOutputCardlist();
    }
}
