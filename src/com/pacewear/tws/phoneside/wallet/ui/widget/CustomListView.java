
package com.pacewear.tws.phoneside.wallet.ui.widget;

import android.content.Context;
import android.util.AttributeSet;

import com.tencent.tws.assistant.widget.ListView;

public class CustomListView extends ListView {

    public CustomListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b + 1);
    }

}
