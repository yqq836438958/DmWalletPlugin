
package com.pacewear.tsm.business;

import android.text.TextUtils;

import com.pacewear.tsm.card.TsmContext;
import com.pacewear.tsm.internal.TsmAppletInstall;
import com.pacewear.tsm.internal.TsmPersonal;
import com.pacewear.tsm.internal.TsmSecDomInstall;
import com.pacewear.tsm.internal.TsmUpdateCardPwd;

import org.json.JSONException;
import org.json.JSONObject;

import TRom.E_INSTALL_SSD_STEP;

public class TsmIssueCard extends TsmBaseBusiness {
    private String mBusinessAID = null;
    private String mBusinessToken = null;
    private String mBusinessExtraInfo = null;

    public TsmIssueCard(TsmContext context, String inputParam) {
        super(context);
        checkEnv(ENV_CHECK_FORCE);
        parseInputParam(inputParam);
    }

    @Override
    protected boolean onStart() {
        if (TextUtils.isEmpty(mBusinessAID)) {
            return false;
        }
        String mainAid = mContext.getCard().getMainAID();
        // String ssd = "0102030405060708";
        String ssd = mContext.getCard().getSSDIDByAppID(mBusinessAID);
        if (!TextUtils.isEmpty(ssd)) {
            // isd,安装辅助安全域
            addProcess(new TsmSecDomInstall(mContext, mainAid, ssd,
                    E_INSTALL_SSD_STEP._EISS_INSTALL_FOR_INSTALL_MAKESELECT));
            // ssd,辅助安全域个人化
            addProcess(new TsmUpdateCardPwd(mContext, ssd));
            // 回到主安全域 移交辅助安全域
            addProcess(new TsmSecDomInstall(mContext, mainAid, ssd,
                    E_INSTALL_SSD_STEP._EISS_INSTALL_FOR_EXTRADITION));
        }
        // 回到辅助安全域 安装应用
        addProcess(new TsmAppletInstall(mContext, ssd, mBusinessAID));
        // 个人化
        addProcess(new TsmPersonal(mContext, mBusinessAID, mBusinessToken, mBusinessExtraInfo));
        // // 设置applet状态
        // new TsmSetAppStatus(mContext, mBusinessAID, E_APP_LIFE_STATUS._EALS_PERSONALIZED));
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
