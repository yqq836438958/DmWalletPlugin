
package com.pacewear.tws.phoneside.wallet.tsm;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.pacewear.tsm.ITsmBusinessListener;
import com.pacewear.tsm.TsmService;
import com.pacewear.tsm.channel.ITsmCardChannel;
import com.pacewear.tws.phoneside.wallet.R;
import com.pacewear.tws.phoneside.wallet.WalletApp;
import com.pacewear.tws.phoneside.wallet.common.Utils;

import org.json.JSONException;
import org.json.JSONObject;

public class TsmTestActivity extends Activity implements OnClickListener, ITsmBusinessListener {
    private Button mIssueCardBtn = null;
    private Button mListStatusBtn = null;
    private Button mShutdownBtn = null;
    private Button mClearAppBtn = null;
    private Button mClearSSDBtn = null;
    private Button mResetAIDBtn = null;
    private Button mSelectAidBtn = null;
    private Button mTopupBtn = null;
    private TextView mResultTextView = null;
    private TsmService mService = null;
    private ITsmCardChannel mChannel = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tsm_test);
        mIssueCardBtn = (Button) findViewById(R.id.issuecard);
        mListStatusBtn = (Button) findViewById(R.id.liststatus);
        mClearAppBtn = (Button) findViewById(R.id.clearapp);
        mClearSSDBtn = (Button) findViewById(R.id.clearssd);
        mShutdownBtn = (Button) findViewById(R.id.shutdown);
        mResetAIDBtn = (Button) findViewById(R.id.resetaid);
        mSelectAidBtn = (Button) findViewById(R.id.selectaid);
        mTopupBtn = (Button) findViewById(R.id.topup);
        mResultTextView = (TextView) findViewById(R.id.result);
        mIssueCardBtn.setOnClickListener(this);
        mListStatusBtn.setOnClickListener(this);
        mClearAppBtn.setOnClickListener(this);
        mShutdownBtn.setOnClickListener(this);
        mClearSSDBtn.setOnClickListener(this);
        mResetAIDBtn.setOnClickListener(this);
        mSelectAidBtn.setOnClickListener(this);
        mTopupBtn.setOnClickListener(this);
        init();
    }

    private void init() {
        mChannel = new SnowBallCardChannel();
        mService = TsmService.getInstance();
        mService.register(WalletApp.sGlobalCtx, mChannel, this);
    }

    @Override
    public void onClick(final View arg0) {
        Utils.getWorkerHandler().removeCallbacksAndMessages(null);
        Utils.getWorkerHandler().post(new Runnable() {
            @Override
            public void run() {
                switch (arg0.getId()) {
                    case R.id.issuecard:
                        mService.issueCard(getIssueParam("00000000BBBBBBBB"));
                        break;
                    case R.id.liststatus:
                        mService.cardListQuery();
                        break;
                    case R.id.clearapp:
                        mService.deleteCard("00000000BBBBBBBB");
                        break;
                    case R.id.clearssd:
                        mService.deleteCard("0102030405060708");
                        break;
                    case R.id.shutdown:
                        mChannel.close();
                        break;
                    case R.id.resetaid:
                        mService.resetAID("0102030405060708");
                        break;
                    case R.id.selectaid:
                        mService.cardSwitch("00000000BBBBBBBB");
                        break;
                    case R.id.topup:
                        mService.cardTopup(getTopupParam("00000000BBBBBBBB"));
                        break;
                    default:
                        break;
                }
            }
        });

    }

    private String getIssueParam(String aid) {
        JSONObject object = new JSONObject();
        try {
            object.put("instance_id", aid);
            object.put("instance_token", "1232222222");
            object.put("extra_info", "aaa");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return object.toString();
    }

    private String getTopupParam(String aid) {
        JSONObject object = new JSONObject();
        try {
            object.put("instance_id", aid);
            object.put("instance_token", "1232222222");
            object.put("extra_info", "aaa");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return object.toString();
    }

    @Override
    public void onSuccess(final String desc) {
        this.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                mResultTextView.setText(desc);
            }
        });

    }

    @Override
    public void onFail(final int error, final String desc) {
        this.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                mResultTextView.setText("error:" + error + ",desc" + desc);
            }
        });

    }

}
