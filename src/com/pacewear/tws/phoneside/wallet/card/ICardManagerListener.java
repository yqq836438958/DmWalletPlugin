
package com.pacewear.tws.phoneside.wallet.card;

import com.pacewear.tws.phoneside.wallet.step.IStep.COMMON_STEP;
import com.pacewear.tws.phoneside.wallet.step.IStep.STATUS;

/**
 * @author baodingzhou
 */

public interface ICardManagerListener {

    /**
     * onCardListMsg
     * 
     * @param step
     * @param status
     */
    public void onCardListMsg(COMMON_STEP step, STATUS status);

    /**
     * onCardMsg
     * 
     * @param aid
     * @param step
     * @param status
     */
    public void onCardMsg(String aid, COMMON_STEP step, STATUS status);
}
