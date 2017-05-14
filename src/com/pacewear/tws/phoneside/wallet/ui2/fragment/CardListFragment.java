
package com.pacewear.tws.phoneside.wallet.ui2.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;

import java.util.ArrayList;
import java.util.List;

public abstract class CardListFragment extends Fragment {
    private CardListFragment mNext = null;
    private FragmentController mController = null;

    private void attachController(FragmentController controller) {
        mController = controller;
    }

    protected final void refreshAllPage() {
        mController.update();
    }

    protected abstract boolean isReady();

    protected abstract void onUpdate();

    public static class FragmentController {
        private int mLayoutId = 0;
        // private FragmentChain mChain = null;
        private FragmentManager mFragmentManager;// fragment管理者
        private List<CardListFragment> mCardListFragments;
        private int mLastCommitFragmentIndex = -1;

        public FragmentController(Activity context, int resLayout, List<CardListFragment> list) {
            mLayoutId = resLayout;
            mFragmentManager = context.getFragmentManager();
            // mChain = new FragmentChain();
            mCardListFragments = list;
            for (CardListFragment fragment : list) {
                // mChain.add(fragment);
                fragment.attachController(this);
                // transaction.add(mLayoutId, fragment);
            }
        }

        public final void update() {
            FragmentTransaction transaction = mFragmentManager.beginTransaction();
            CardListFragment target = null;
            int size = mCardListFragments.size();
            for (int i = 0; i < size; i++) {
                target = mCardListFragments.get(i);
                if (!target.isReady()) {
                    continue;
                }
                if (mLastCommitFragmentIndex != i) {
                    transaction.replace(mLayoutId, target);
                }
                target.onUpdate();
                mLastCommitFragmentIndex = i;
            }
            transaction.commitAllowingStateLoss();
        }
    }

}
