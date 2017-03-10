
package com.pacewear.tsm.business;

import com.pacewear.tsm.card.CardListItem;
import com.pacewear.tsm.card.TsmContext;
import com.pacewear.tsm.common.APDUUtil;
import com.pacewear.tsm.common.Constants;
import com.pacewear.tsm.internal.TsmActiveApp;
import com.pacewear.tsm.internal.TsmListState;

import java.util.List;

public class TsmCardSwitch extends TsmBaseBusiness {
    private String mInstanceId = null;

    public TsmCardSwitch(TsmContext ctx, String aid) {
        super(ctx);
        mInstanceId = aid;
        checkEnv(ENV_CHECK_SKIP);
    }

    @Override
    protected boolean onStart() {
        addProcess(
                new TsmListState(mContext, Constants.TSM_CRS_AID, getCustomListStatAPDU())); // TODO
        List<CardListItem> list = mContext.getCard().getExistCardList();
        for (CardListItem item : list) {
            if (!item.aid.equalsIgnoreCase(mInstanceId)) {
                addProcess(
                        new TsmActiveApp(mContext, Constants.TSM_CRS_AID, item.aid, false));
            }
        }
        addProcess(new TsmActiveApp(mContext, Constants.TSM_CRS_AID, mInstanceId,
                true));
        return true;
    }

    private String getCustomListStatAPDU() {
        List<CardListItem> cardListItems = mContext.getCard().getExistCardList();
        if (cardListItems != null && cardListItems.size() == 1) {
            return APDUUtil.getCRSAppStat(cardListItems.get(0).aid);
        }
        return APDUUtil.listCRSApp();
    }
}
