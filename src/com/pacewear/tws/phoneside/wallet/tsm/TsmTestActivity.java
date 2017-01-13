
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
import com.pacewear.tsm.common.CacheUtil;
import com.pacewear.tsm.query.TsmApplet.AppletTagQuery;
import com.pacewear.tws.phoneside.wallet.R;
import com.pacewear.tws.phoneside.wallet.WalletApp;
import com.pacewear.tws.phoneside.wallet.common.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class TsmTestActivity extends Activity implements OnClickListener, ITsmBusinessListener {
    private Button mIssueCardBtn = null;
    private Button mListStatusBtn = null;
    private Button mShutdownBtn = null;
    private Button mClearAppBtn = null;
    private Button mClearSSDBtn = null;
    private Button mResetAIDBtn = null;
    private Button mSelectAidBtn = null;
    private Button mTopupBtn = null;
    private Button mCardQueryBtn = null;
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
        mCardQueryBtn = (Button) findViewById(R.id.cardinfoquery);
        mResultTextView = (TextView) findViewById(R.id.result);
        mIssueCardBtn.setOnClickListener(this);
        mListStatusBtn.setOnClickListener(this);
        mClearAppBtn.setOnClickListener(this);
        mShutdownBtn.setOnClickListener(this);
        mClearSSDBtn.setOnClickListener(this);
        mResetAIDBtn.setOnClickListener(this);
        mSelectAidBtn.setOnClickListener(this);
        mTopupBtn.setOnClickListener(this);
        mCardQueryBtn.setOnClickListener(this);
        init();
    }

    private void init() {
        mChannel = new SnowBallCardChannel();
        mService = TsmService.getInstance();
        mService.register(WalletApp.sGlobalCtx, mChannel, this);
        Utils.getWorkerHandler().post(new Runnable() {

            @Override
            public void run() {
                AppletTagQuery tagQuery = new AppletTagQuery("9156000014010001");
                List<String> list1 = new ArrayList<String>();
                list1.add("00A40000023F00");
                list1.add("00B0840000");
                // list1.add("805C000204");
                tagQuery.put("card_number", list1);
                List<String> list2 = new ArrayList<String>();
                // list2.add("00A40000023F00");
                list2.add("00A40000021001");
                list2.add("805C000204");
                tagQuery.put("amount", list2);
                CacheUtil.save("card_query_aid_9156000014010001", tagQuery);
            }
        });
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
                    case R.id.cardinfoquery:
                        mService.cardQuery(getCardQueryParam("9156000014010001"));
                        break;
                    default:
                        break;
                }
            }
        });

    }

    private String getCardQueryParam(String aid) {
        JSONObject object = new JSONObject();
        try {
            object.put("instance_id", aid);
            object.put("tag", "amount,card_number");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return object.toString();
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
