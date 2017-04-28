
package com.pacewear.tws.phoneside.wallet.ui2.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;

import java.util.ArrayList;
import java.util.List;

public abstract class CardListFragment extends Fragment {
    private CardListFragment mNext = null;

    CardListFragment update() {
        if (onUpdate()) {
            return this;
        }
        if (mNext != null) {
            return mNext.update();
        }
        return null;
    }

    final void setNext(CardListFragment next) {
        this.mNext = next;
    }

    protected abstract boolean onUpdate();

    static class FragmentChain {
        private List<CardListFragment> mList = new ArrayList<CardListFragment>();
        private int mSize = 0;

        public final void add(CardListFragment node) {
            if (node == null) {
                return;
            }
            synchronized (FragmentChain.this) {
                if (mSize > 0) {
                    mList.get(mSize - 1).setNext(node);
                }
                mList.add(node);
                mSize++;
            }
        }

        public final CardListFragment update() {
            if (mList == null || mList.size() <= 0) {
                return null;
            }
            return mList.get(0).update();
        }
    }

    public static class FragmentController {
        private int mLayoutId = 0;
        private FragmentChain mChain = null;
        private FragmentManager mFragmentManager;// fragment管理者
        private List<CardListFragment> mCardListFragments;

        public FragmentController(Activity context, int resLayout, List<CardListFragment> list) {
            mLayoutId = resLayout;
            mFragmentManager = context.getFragmentManager();
            mChain = new FragmentChain();
            mCardListFragments = list;
            FragmentTransaction transaction = mFragmentManager.beginTransaction();
            for (CardListFragment fragment : list) {
                mChain.add(fragment);
                transaction.add(mLayoutId, fragment);
            }
            transaction.commit();
        }

        public final void update() {
            FragmentTransaction transaction = mFragmentManager.beginTransaction();
            CardListFragment target = mChain.update();
            for (CardListFragment fragment : mCardListFragments) {
                if (!fragment.equals(target)) {
                    transaction.hide(fragment);
                } else {
                    transaction.show(target);
                }
            }
            transaction.commit();
        }
    }
}
