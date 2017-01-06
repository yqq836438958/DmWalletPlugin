
package com.pacewear.tsm.business;

import android.text.TextUtils;

import com.pacewear.tsm.card.TsmContext;
import com.pacewear.tsm.internal.TsmAppInfoQuery;
import com.pacewear.tsm.internal.TsmSelectAID;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class TsmCardQuery extends TsmBaseBusiness {
    private String mInstanceAID = null;
    private List<String> mTagList = new ArrayList<String>();

    public TsmCardQuery(TsmContext ctx, String input) {
        super(ctx);
        parseInputParam(input);
    }

    @Override
    protected boolean onStart() {
        if (TextUtils.isEmpty(mInstanceAID)) {
            return false;
        }
        addProcess(new TsmSelectAID(mContext, mInstanceAID));
        addProcess(new TsmAppInfoQuery(mContext, mInstanceAID, mTagList));
        return true;
    }

    private void parseInputParam(String input) {
        try {
            JSONObject object = new JSONObject(input);
            mInstanceAID = object.optString("instance_id");
            String val = object.optString("tag");
            String[] tmpArr = val.split(",");
            for (String tag : tmpArr) {
                mTagList.add(tag);
            }
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
