
package com.pacewear.tsm;

import android.content.Context;

import com.pacewear.tsm.business.TsmBaseBusiness;
import com.pacewear.tsm.business.TsmCardListQuery;
import com.pacewear.tsm.business.TsmCardQuery;
import com.pacewear.tsm.business.TsmCardSwitch;
import com.pacewear.tsm.business.TsmCardTopup;
import com.pacewear.tsm.business.TsmIssueCard;
import com.pacewear.tsm.business.TsmRemoveApp;
import com.pacewear.tsm.business.TsmResetAID;
import com.pacewear.tsm.card.TsmContext;
import com.pacewear.tsm.channel.ITsmCardChannel;

public class TsmService {
    private static TsmService sInstance = null;
    private Context mAppContext = null;
    private TsmContext mTsmContext = null;

    public static TsmService getInstance() {
        if (sInstance == null) {
            synchronized (TsmService.class) {
                sInstance = new TsmService();
            }
        }
        return sInstance;
    }

    private TsmService() {
        mTsmContext = new TsmContext();
    }

    public void register(Context ctx, ITsmCardChannel channel, ITsmBusinessListener callback) {
        mAppContext = ctx;
        mTsmContext.setChannel(channel);
        mTsmContext.setBusinessListener(callback);
    }

    public Context getContext() {
        return mAppContext;
    }

    public void issueCard(String input) {
        TsmBaseBusiness issuecard = new TsmIssueCard(mTsmContext, input);
        issuecard.start();
    }

    public void deleteCard(String aid) {
        TsmBaseBusiness delete = new TsmRemoveApp(mTsmContext, aid);
        delete.start();
    }

    public void cardListQuery() {
        TsmBaseBusiness cardList = new TsmCardListQuery(mTsmContext);
        cardList.start();
    }

    public void cardSwitch(String aid) {
        TsmBaseBusiness cardSwitch = new TsmCardSwitch(mTsmContext, aid);
        cardSwitch.start();
    }

    public void cardTopup(String input) {
        TsmBaseBusiness cardTopup = new TsmCardTopup(mTsmContext, input);
        cardTopup.start();
    }

    public void cardQuery(String input) {
        TsmBaseBusiness cardQuery = new TsmCardQuery(mTsmContext, input);
        cardQuery.start();
    }

    public void resetAID(String aid) {
        TsmBaseBusiness reset = new TsmResetAID(mTsmContext, aid);
        reset.start();
    }
}
