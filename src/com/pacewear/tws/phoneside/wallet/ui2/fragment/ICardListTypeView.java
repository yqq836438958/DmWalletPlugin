
package com.pacewear.tws.phoneside.wallet.ui2.fragment;

public interface ICardListTypeView {
    public static interface ICardListEvent {
        public void onConsume();
    }

    public int update(); // 如果返回 -1，这里协议，当前page应该被隐藏
}
