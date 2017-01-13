
package com.pacewear.tsm.card;

public class CardListItem {
    public String aid;
    public int iInstallStat;
    public boolean bActive = true;

    public CardListItem(String aid, int stall, boolean bactive) {
        this(aid, stall);
        this.bActive = bactive;
    }

    public CardListItem(String aid, int install) {
        this.aid = aid;
        this.iInstallStat = install;
    }
}
