
package com.pacewear.tws.phoneside.wallet.card;

import com.pacewear.tws.phoneside.wallet.step.IStep.COMMON_STEP;
import com.pacewear.tws.phoneside.wallet.step.IStep.STATUS;

/**
 * @author baodingzhou
 */

public interface ICardManagerInner extends ICardManager {

    /**
     * notifyCardListMsg
     * 
     * @param step
     * @param status
     */
    public void notifyCardListMsg(COMMON_STEP step, STATUS status);

    /**
     * notifyCardMsg
     * 
     * @param aid
     * @param step
     * @param status
     */
    public void notifyCardMsg(String aid, COMMON_STEP step, STATUS status);
}
