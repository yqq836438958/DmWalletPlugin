
package com.pacewear.tws.phoneside.wallet.present;

import com.pacewear.tws.phoneside.wallet.card.ICard;

import java.util.List;

public interface ICardModulePresent {
    public void cardListQuery();

    public boolean isReady();

    public List<ICard> getCardList();

    public ICard getCard(String aid);

    public void cardSwitch(String aid);
}
