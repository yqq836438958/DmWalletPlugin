
package com.pacewear.tsm.card;

import com.pacewear.tsm.ITsmBusinessListener;
import com.pacewear.tsm.channel.ITsmCardChannel;

import TRom.CardStatusContext;

public class TsmContext {

    private ITsmCardChannel mCardChannel = null;
    private String mSessionId = "";
    private ITsmBusinessListener mBusinessListener;
    private TsmCard mTsmCard = null;

    public TsmContext() {
        mTsmCard = new TsmCard();
    }

    public void syncCardStatus(CardStatusContext ctx) {
        if (mTsmCard != null) {
            mTsmCard.update(ctx);
        }

    }

    public void setChannel(ITsmCardChannel channel) {
        mCardChannel = channel;
    }

    public ITsmCardChannel getChannel() {
        return mCardChannel;
    }

    public void setSessionId(String sessionId) {
        mSessionId = sessionId;
    }

    public String getSessionId() {
        return mSessionId;
    }

    public void setCplc(String cplc) {
        if (cplc.equalsIgnoreCase(mTsmCard.getCPLC())) {
            return;
        }
        mTsmCard.setCPLC(cplc);
    }

    public TsmCard getCard() {
        return mTsmCard;
    }

    public void reset() {
        mSessionId = null;
        if (mTsmCard != null) {
            mTsmCard.clear();
        }
    }

    public void setBusinessListener(ITsmBusinessListener callback) {
        mBusinessListener = callback;
    }

    public ITsmBusinessListener getITsmBusinessListener() {
        return mBusinessListener;
    }

}
