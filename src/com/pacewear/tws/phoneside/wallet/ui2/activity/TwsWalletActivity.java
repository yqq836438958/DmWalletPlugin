
package com.pacewear.tws.phoneside.wallet.ui2.activity;

import android.app.TwsActivity;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;

import com.pacewear.tws.phoneside.wallet.R;
import com.pacewear.tws.phoneside.wallet.common.ClickFilter;
import com.tencent.tws.assistant.app.ActionBar;

public class TwsWalletActivity extends TwsActivity {
    private View.OnClickListener mMoreOptionClickEvent = new OnClickListener() {

        @Override
        public void onClick(View v) {
            if (ClickFilter.isMultiClick()) {
                return;
            }
            onMoreOptionClick();
        }
    };

    protected final void setActionBar(int resTitle, IActionBarStagy stagy) {
        String title = getString(resTitle);
        setActionBar(title, stagy);
    }

    protected final void setActionBar(String title, IActionBarStagy stagy) {
        stagy.onCall(title);
    }

    protected final void setActionBar(int resTitle) {
        setActionBar(getString(resTitle));
    }

    protected final void setActionBar(String title) {
        IActionBarStagy noTitle = new NormalStagy();
        noTitle.onCall(title);
    }

    protected final void hideActionBar() {
        IActionBarStagy noTitle = new NoTitleStagy();
        noTitle.onCall("");
    }

    protected interface IActionBarStagy {
        public void onCall(String title);
    }

    protected class NormalStagy implements IActionBarStagy {
        @Override
        public void onCall(String title) {
            ActionBar actionBar = getTwsActionBar();
            actionBar.setTitle(title);
        }
    }

    protected class LeftCloseTextStagy implements IActionBarStagy {

        @Override
        public void onCall(String title) {
            ActionBar actionBar = getActionInternal(title);
            setLeftCancle(R.string.wallet_operation_result_close, actionBar);
        }

    }

    protected class RightHelpStagy implements IActionBarStagy {
        @Override
        public void onCall(String title) {
            ActionBar actionBar = getActionInternal(title);
            setRightHelpIcon(actionBar);
        }
    }

    protected class RightMoreOptionStagy implements IActionBarStagy {
        @Override
        public void onCall(String title) {
            ActionBar actionBar = getActionInternal(title);
            setMoreAction(actionBar);
        }
    }

    protected class NoTitleStagy implements IActionBarStagy {
        @Override
        public void onCall(String title) {
            ActionBar actionBar = getActionInternal(title);
            actionBar.hide();
        }
    }

    protected class LeftCancleStagy implements IActionBarStagy {
        @Override
        public void onCall(String title) {
            ActionBar actionBar = getActionInternal(title);
            setLeftCancle(R.string.wallet_cancel, actionBar);
        }
    }

    protected class LeftCancleRightHelpStagy implements IActionBarStagy {
        @Override
        public void onCall(String title) {
            ActionBar actionBar = getActionInternal(title);
            setLeftCancle(R.string.wallet_cancel, actionBar);
            setRightHelpIcon(actionBar);
        }
    }

    protected class LeftCancleRightHelpNoTitleStagy implements IActionBarStagy {
        @Override
        public void onCall(String title) {
            ActionBar actionBar = getActionInternal("");
            actionBar.setBackgroundDrawable(null);
            setLeftCancle(R.string.wallet_cancel, actionBar);
            setRightHelpIcon(actionBar);
        }
    }

    protected class NoTitleNoHideStagy implements IActionBarStagy {

        @Override
        public void onCall(String title) {
            ActionBar actionBar = getActionInternal("");
            actionBar.setBackgroundDrawable(null);
            actionBar.getCloseView(false).setVisibility(View.GONE);
        }

    }

    private ActionBar getActionInternal(String title) {
        ActionBar actionBar = getTwsActionBar();
        actionBar.setTitle(title);
        return actionBar;
    }

    private void setRightHelpIcon(ActionBar actionBar) {
        ImageView help = actionBar.getRightButtonView();
        help.setImageResource(R.drawable.wallet_actionbar_help_bg);
        help.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoHelpPage();
            }
        });
    }

    private void setLeftCancle(int leftTextRes, ActionBar actionBar) {
        Button close = (Button) actionBar.getCloseView(false);
        close.setText(getResources().getString(leftTextRes));
        close.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void setMoreAction(ActionBar actionBar) {
        ImageView btn = (ImageView) actionBar.getRightButtonView();
        btn.setImageResource(R.drawable.wallet_action_more_option_bg);
        btn.setOnClickListener(mMoreOptionClickEvent);
    }

    protected void gotoHelpPage() {
        Intent intent = new Intent(TwsWalletActivity.this, HelpActivity.class);
        intent.putExtra(HelpActivity.KEY_HELP,
                getString(R.string.wallet_trafficcard_usinghelp_url));
        startActivity(intent);
    }

    protected void onMoreOptionClick() {

    }
}
