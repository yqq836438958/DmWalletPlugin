
package com.pacewear.tws.phoneside.wallet.ui.widget;

import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;

import com.tencent.tws.assistant.app.AlertDialog;
import com.tencent.tws.assistant.app.TwsDialog;

import java.util.ArrayList;
import java.util.List;

public class TwsDialogController {
    public static TwsDialog newDialog() {
        return null;
    }

    private AlertDialog.Builder mBuilder = null;
    private List<OnItemEvent> mEvents = null;
    private List<String> mResArray = null;

    public static interface OnItemEvent {
        public void onHandle();
    }

    public TwsDialogController(Context context, boolean isBottom) {
        mBuilder = new AlertDialog.Builder(context, isBottom);
        mEvents = new ArrayList<TwsDialogController.OnItemEvent>();
        mResArray = new ArrayList<String>();
    }

    public TwsDialogController withIcon(int res) {
        mBuilder.setIcon(res);
        return this;
    }

    public TwsDialogController withTitle(String str) {
        mBuilder.setTitle(str);
        return this;
    }

    public TwsDialogController addItem(String str, OnItemEvent event) {
        mResArray.add(str);
        mEvents.add(event);
        return this;
    }

    private void wrapEventInternalByRes(int resArray, final OnItemEvent[] events) {
        mBuilder.setBottomButtonItems(resArray, new OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                onHandleEvent(events, which);
            }
        });
    }

    private void wrapEvnetInternalByStr(String[] items, final OnItemEvent[] events) {
        mBuilder.setBottomButtonItems(items, new OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                onHandleEvent(events, which);
            }
        });
    }

    private void onHandleEvent(OnItemEvent[] events, int which) {
        if (events == null || which >= events.length) {
            return;
        }
        if (which < events.length && which >= 0 && events[which] != null) {
            events[which].onHandle();
        }
    }

    public TwsDialogController fillItems(int resArray, OnItemEvent[] events) {
        wrapEventInternalByRes(resArray, events);
        return this;
    }

    public TwsDialogController fillItems(String[] items, OnItemEvent[] events) {
        if (items == null) {
            return this;
        }
        wrapEvnetInternalByStr(items, events);
        return this;
    }

    public AlertDialog flush() {
        if (mResArray.size() > 0) {
            wrapEvnetInternalByStr((String[]) mResArray.toArray(),
                    (OnItemEvent[]) mEvents.toArray());
        }
        return mBuilder.create(true);
    }

}
