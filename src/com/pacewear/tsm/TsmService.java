
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

    public void register(Context ctx,
            ITsmCardChannel channel/* , ITsmBusinessListener callback */) {
        mAppContext = ctx;
        mTsmContext.setChannel(channel);
    }

    public Context getContext() {
        return mAppContext;
    }

    public int issueCard(String input,ITsmBusinessListener callback) {
        mTsmContext.setBusinessListener(callback);
        TsmBaseBusiness issuecard = new TsmIssueCard(mTsmContext, input);
        issuecard.start();
        return 0;
    }

    public int deleteCard(String aid,ITsmBusinessListener callback) {
        mTsmContext.setBusinessListener(callback);
        TsmBaseBusiness delete = new TsmRemoveApp(mTsmContext, aid);
        delete.start();
        return 0;
    }

    public int cardListQuery(ITsmBusinessListener callback) {
        mTsmContext.setBusinessListener(callback);
        TsmBaseBusiness cardList = new TsmCardListQuery(mTsmContext);
        cardList.start();
        return 0;
    }

    public int cardSwitch(String aid,ITsmBusinessListener callback) {
        mTsmContext.setBusinessListener(callback);
        TsmBaseBusiness cardSwitch = new TsmCardSwitch(mTsmContext, aid);
        cardSwitch.start();
        return 0;
    }

    public int cardTopup(String input,ITsmBusinessListener callback) {
        mTsmContext.setBusinessListener(callback);
        TsmBaseBusiness cardTopup = new TsmCardTopup(mTsmContext, input);
        cardTopup.start();
        return 0;
    }

    public int cardQuery(String input,ITsmBusinessListener callback) {
        mTsmContext.setBusinessListener(callback);
        TsmBaseBusiness cardQuery = new TsmCardQuery(mTsmContext, input);
        cardQuery.start();
        return 0;
    }

    public int resetAID(String aid,ITsmBusinessListener callback) {
        mTsmContext.setBusinessListener(callback);
        TsmBaseBusiness reset = new TsmResetAID(mTsmContext, aid);
        reset.start();
        return 0;
    }
}
