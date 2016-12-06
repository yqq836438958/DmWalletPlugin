
package com.pacewear.tws.phoneside.wallet.card;

import com.pacewear.tws.phoneside.wallet.card.ICard.CARD_TYPE;
import com.pacewear.tws.phoneside.wallet.card.ICard.INSTALL_STATUS;

import java.util.ArrayList;

/**
 * @author baodingzhou
 */

public interface ICardManager {

    /**
     * registerCardManagerListener
     * 
     * @param listener
     * @return
     */
    public boolean registerCardManagerListener(ICardManagerListener listener);

    /**
     * unregisterCardManagerListener
     * 
     * @param listener
     * @return
     */
    public boolean unregisterCardManagerListener(ICardManagerListener listener);

    /**
     * isReady
     * 
     * @return
     */
    public boolean isReady();

    /**
     * isAvaliable
     * 
     * @return
     */
    public boolean isAvaliable();

    /**
     * forceQuery
     * 
     * @return
     */
    public boolean forceUpdate(boolean isForce);

    /**
     * getCard
     * 
     * @param aid
     * @return
     */
    public ICard getCard(String aid);

    /**
     * getCard
     * 
     * @return
     */
    public ICard[] getCard();

    /**
     * getCard
     * 
     * @param type
     * @return
     */
    public ArrayList<ICard> getCard(CARD_TYPE type);

    /**
     * getCard
     * 
     * @param installStatus
     * @return
     */
    public ArrayList<ICard> getCard(INSTALL_STATUS installStatus);

    /**
     * setDefaultCard
     * 
     * @param aid
     * @return
     */
    public boolean setDefaultCard(String aid);

    /**
     * isOverMaxQueryTimes
     *
     * @return
     */
    public boolean isOverMaxQueryTimes();

    /**
     * isInSyncProcess
     *
     * @return
     */
    public boolean isInSyncProcess();
}
