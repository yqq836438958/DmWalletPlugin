
package com.pacewear.tws.phoneside.wallet.present;

public interface ICardListPresent {
    public void cardListQuery();

    public void onPageUpdate();

    public boolean isCardListReady();

    public int size();
}
