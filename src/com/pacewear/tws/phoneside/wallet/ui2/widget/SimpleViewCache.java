
package com.pacewear.tws.phoneside.wallet.ui2.widget;

import android.view.View;

public class SimpleViewCache {

    private View baseView;

    public SimpleViewCache() {
    }

    public SimpleViewCache(View baseView) {
        this.baseView = baseView;
    }

    public View getBaseView() {
        return baseView;
    }

    public void setBaseView(View baseView) {
        this.baseView = baseView;
    }

}
