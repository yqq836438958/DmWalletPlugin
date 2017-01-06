
package com.pacewear.tsm.business;

import com.pacewear.tsm.card.TsmContext;
import com.pacewear.tsm.internal.TsmSelectAID;
import com.pacewear.tsm.internal.TsmTopup;

import org.json.JSONException;
import org.json.JSONObject;

public class TsmCardTopup extends TsmBaseBusiness {
    private String mBusinessAID;
    private String mBusinessToken;
    private String mBusinessExtraInfo;

    public TsmCardTopup(TsmContext ctx, String inPutParam) {
        super(ctx);
        parseInputParam(inPutParam);
    }

    @Override
    protected boolean onStart() {
        addProcess(new TsmSelectAID(mContext, mBusinessAID));
        addProcess(new TsmTopup(mContext, mBusinessAID, mBusinessToken, mBusinessExtraInfo));
        return true;
    }

    private void parseInputParam(String param) {
        try {
            JSONObject object = new JSONObject(param);
            mBusinessAID = object.optString("instance_id");
            mBusinessToken = object.optString("instance_token");
            mBusinessExtraInfo = object.optString("extra_info");
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
