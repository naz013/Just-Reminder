package com.cray.software.justreminder.interfaces;

import android.animation.ObjectAnimator;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.cray.software.justreminder.adapters.RemindersRecyclerAdapter;
import com.cray.software.justreminder.utils.QuickReturnUtils;

import java.util.ArrayList;
import java.util.List;

public class QuickReturnRecyclerViewOnScrollListener extends RecyclerView.OnScrollListener {

    // region Member Variables
    private final QuickReturnViewType mQuickReturnViewType;
    private final View mHeader;
    private final int mMinHeaderTranslation;
    private final View mFooter;
    private final int mMinFooterTranslation;
    private final boolean mIsSnappable; // Can Quick Return view snap into place?
    private final boolean mIsGrid;
    private RemindersRecyclerAdapter mAdapter;

    private int mPrevScrollY = 0;
    private int mHeaderDiffTotal = 0;
    private int mFooterDiffTotal = 0;
    private final int mColumnCount;
    private CollapseListener mListener = null;
    private List<RecyclerView.OnScrollListener> mExtraOnScrollListenerList = new ArrayList<>();
    // endregion

    // region Constructors
    private QuickReturnRecyclerViewOnScrollListener(Builder builder) {
        mQuickReturnViewType = builder.mQuickReturnViewType;
        mHeader = builder.mHeader;
        mMinHeaderTranslation = builder.mMinHeaderTranslation;
        mFooter = builder.mFooter;
        mListener = builder.mListener;
        mColumnCount = builder.mColumnCount;
        mMinFooterTranslation = builder.mMinFooterTranslation;
        mIsSnappable = builder.mIsSnappable;
        mIsGrid = builder.isGrid;
        mAdapter = builder.adapter;
    }
    // endregion

    @Override
    public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
        Log.d(getClass().getSimpleName(), "onScrollStateChanged() : scrollState - " + newState);
        // apply another list' s on scroll listener
        for (RecyclerView.OnScrollListener listener : mExtraOnScrollListenerList) {
            listener.onScrollStateChanged(recyclerView, newState);
        }

        if (mListener != null) mListener.onStartScroll(true);
        if(newState == RecyclerView.SCROLL_STATE_IDLE && mIsSnappable){

            if (mAdapter != null) mAdapter.setScrolled(true);
            int midHeader = -mMinHeaderTranslation/2;
            int midFooter = mMinFooterTranslation/2;

            switch (mQuickReturnViewType) {
                case HEADER:
                    if (-mHeaderDiffTotal > 0 && -mHeaderDiffTotal < midHeader) {
                        ObjectAnimator anim = ObjectAnimator.ofFloat(mHeader, "translationY", mHeader.getTranslationY(), 0);
                        anim.setDuration(100);
                        anim.start();
                        mHeaderDiffTotal = 0;
                    } else if (-mHeaderDiffTotal < -mMinHeaderTranslation && -mHeaderDiffTotal >= midHeader) {
                        ObjectAnimator anim = ObjectAnimator.ofFloat(mHeader, "translationY", mHeader.getTranslationY(), mMinHeaderTranslation);
                        anim.setDuration(100);
                        anim.start();
                        mHeaderDiffTotal = mMinHeaderTranslation;
                    }
                    break;
                case FOOTER:
                    if (-mFooterDiffTotal > 0 && -mFooterDiffTotal < midFooter) { // slide up
                        ObjectAnimator anim = ObjectAnimator.ofFloat(mFooter, "translationY", mFooter.getTranslationY(), 0);
                        anim.setDuration(100);
                        anim.start();
                        mFooterDiffTotal = 0;
                    } else if (-mFooterDiffTotal < mMinFooterTranslation && -mFooterDiffTotal >= midFooter) { // slide down
                        ObjectAnimator anim = ObjectAnimator.ofFloat(mFooter, "translationY", mFooter.getTranslationY(), mMinFooterTranslation);
                        anim.setDuration(100);
                        anim.start();
                        mFooterDiffTotal = -mMinFooterTranslation;
                    }
                    break;
                case BOTH:
                    if (-mHeaderDiffTotal > 0 && -mHeaderDiffTotal < midHeader) {
                        ObjectAnimator anim = ObjectAnimator.ofFloat(mHeader, "translationY", mHeader.getTranslationY(), 0);
                        anim.setDuration(100);
                        anim.start();
                        mHeaderDiffTotal = 0;
                    } else if (-mHeaderDiffTotal < -mMinHeaderTranslation && -mHeaderDiffTotal >= midHeader) {
                        ObjectAnimator anim = ObjectAnimator.ofFloat(mHeader, "translationY", mHeader.getTranslationY(), mMinHeaderTranslation);
                        anim.setDuration(100);
                        anim.start();
                        mHeaderDiffTotal = mMinHeaderTranslation;
                    }

                    if (-mFooterDiffTotal > 0 && -mFooterDiffTotal < midFooter) { // slide up
                        ObjectAnimator anim = ObjectAnimator.ofFloat(mFooter, "translationY", mFooter.getTranslationY(), 0);
                        anim.setDuration(100);
                        anim.start();
                        mFooterDiffTotal = 0;
                    } else if (-mFooterDiffTotal < mMinFooterTranslation && -mFooterDiffTotal >= midFooter) { // slide down
                        ObjectAnimator anim = ObjectAnimator.ofFloat(mFooter, "translationY", mFooter.getTranslationY(), mMinFooterTranslation);
                        anim.setDuration(100);
                        anim.start();
                        mFooterDiffTotal = -mMinFooterTranslation;
                    }
                    break;
                case TWITTER:
                    if (-mHeaderDiffTotal > 0 && -mHeaderDiffTotal < midHeader) {
                        ObjectAnimator anim = ObjectAnimator.ofFloat(mHeader, "translationY", mHeader.getTranslationY(), 0);
                        anim.setDuration(100);
                        anim.start();
                        mHeaderDiffTotal = 0;
                    } else if (-mHeaderDiffTotal < -mMinHeaderTranslation && -mHeaderDiffTotal >= midHeader) {
                        ObjectAnimator anim = ObjectAnimator.ofFloat(mHeader, "translationY", mHeader.getTranslationY(), mMinHeaderTranslation);
                        anim.setDuration(100);
                        anim.start();
                        mHeaderDiffTotal = mMinHeaderTranslation;
                    }

                    if (-mFooterDiffTotal > 0 && -mFooterDiffTotal < midFooter) { // slide up
                        ObjectAnimator anim = ObjectAnimator.ofFloat(mFooter, "translationY", mFooter.getTranslationY(), 0);
                        anim.setDuration(100);
                        anim.start();
                        mFooterDiffTotal = 0;
                    } else if (-mFooterDiffTotal < mMinFooterTranslation && -mFooterDiffTotal >= midFooter) { // slide down
                        ObjectAnimator anim = ObjectAnimator.ofFloat(mFooter, "translationY", mFooter.getTranslationY(), mMinFooterTranslation);
                        anim.setDuration(100);
                        anim.start();
                        mFooterDiffTotal = -mMinFooterTranslation;
                    }
                    break;
            }

        } else {
            if (mAdapter != null) mAdapter.setScrolled(false);
        }
        if (newState == 0) {
            if (mAdapter != null) mAdapter.setScrolled(false);
        }
    }

    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        // apply extra on scroll listener
        for (RecyclerView.OnScrollListener listener : mExtraOnScrollListenerList) {
            /*RecyclerView.LayoutManager manager = recyclerView.getLayoutManager();
            int firstVisibleItem = 0;
            int visibleItemCount = 0;
            int totalItemCount = 0;
            if (manager != null){
                totalItemCount = manager.getItemCount();
                visibleItemCount = manager.getChildCount();
            }*/
            listener.onScrolled(recyclerView, dx, dy);
        }
        int scrollY = QuickReturnUtils.getScrollY(recyclerView, mColumnCount, mIsGrid);
        int diff = mPrevScrollY - scrollY;

//        Log.d(getClass().getSimpleName(), "onScroll() : scrollY - "+scrollY);
//        Log.d(getClass().getSimpleName(), "onScroll() : diff - "+diff);
//        Log.d(getClass().getSimpleName(), "onScroll() : mMinHeaderTranslation - "+mMinHeaderTranslation);
//        Log.d(getClass().getSimpleName(), "onScroll() : mMinFooterTranslation - "+mMinFooterTranslation);

        if(diff != 0){
            if (mAdapter != null) mAdapter.setScrolled(true);
            switch (mQuickReturnViewType){
                case HEADER:
                    if(diff < 0){ // scrolling down
                        mHeaderDiffTotal = Math.max(mHeaderDiffTotal + diff, mMinHeaderTranslation);
                    } else { // scrolling up
                        mHeaderDiffTotal = Math.min(Math.max(mHeaderDiffTotal + diff, mMinHeaderTranslation), 0);
                    }

                    mHeader.setTranslationY(mHeaderDiffTotal);
                    break;
                case FOOTER:
                    if(diff < 0){ // scrolling down
                        mFooterDiffTotal = Math.max(mFooterDiffTotal + diff, -mMinFooterTranslation);
                    } else { // scrolling up
                        mFooterDiffTotal = Math.min(Math.max(mFooterDiffTotal + diff, -mMinFooterTranslation), 0);
                    }

                    mFooter.setTranslationY(-mFooterDiffTotal);
                    break;
                case BOTH:
                    if(diff < 0){ // scrolling down
                        mHeaderDiffTotal = Math.max(mHeaderDiffTotal + diff, mMinHeaderTranslation);
                        mFooterDiffTotal = Math.max(mFooterDiffTotal + diff, -mMinFooterTranslation);
                    } else { // scrolling up
                        mHeaderDiffTotal = Math.min(Math.max(mHeaderDiffTotal + diff, mMinHeaderTranslation), 0);
                        mFooterDiffTotal = Math.min(Math.max(mFooterDiffTotal + diff, -mMinFooterTranslation), 0);
                    }

                    mHeader.setTranslationY(mHeaderDiffTotal);
                    mFooter.setTranslationY(-mFooterDiffTotal);
                    break;
                case TWITTER:
                    if(diff < 0){ // scrolling down
                        if(scrollY > -mMinHeaderTranslation)
                            mHeaderDiffTotal = Math.max(mHeaderDiffTotal + diff, mMinHeaderTranslation);

                        if(scrollY > mMinFooterTranslation)
                            mFooterDiffTotal = Math.max(mFooterDiffTotal + diff, -mMinFooterTranslation);
                    } else { // scrolling up
                        mHeaderDiffTotal = Math.min(Math.max(mHeaderDiffTotal + diff, mMinHeaderTranslation), 0);
                        mFooterDiffTotal = Math.min(Math.max(mFooterDiffTotal + diff, -mMinFooterTranslation), 0);
                    }

                    mHeader.setTranslationY(mHeaderDiffTotal);
                    mFooter.setTranslationY(-mFooterDiffTotal);
                default:
                    break;
            }
        } else {
            if (mAdapter != null) mAdapter.setScrolled(false);
        }

        mPrevScrollY = scrollY;
    }

    // region Helper Methods
    public void registerExtraOnScrollListener(RecyclerView.OnScrollListener listener) {
        mExtraOnScrollListenerList.add(listener);
    }
    // endregion

    // region Inner Classes

    public static class Builder {
        // Required parameters
        private final QuickReturnViewType mQuickReturnViewType;

        // Optional parameters - initialized to default values
        private View mHeader = null;
        private int mMinHeaderTranslation = 0;
        private View mFooter = null;
        private int mMinFooterTranslation = 0;
        private boolean mIsSnappable = false;
        private boolean isGrid = false;
        private int mColumnCount = 1;
        private CollapseListener mListener = null;
        private RemindersRecyclerAdapter adapter = null;

        public Builder(QuickReturnViewType quickReturnViewType) {
            mQuickReturnViewType = quickReturnViewType;
        }

        public Builder header(View header){
            mHeader = header;
            return this;
        }

        public Builder minHeaderTranslation(int minHeaderTranslation){
            mMinHeaderTranslation = minHeaderTranslation;
            return this;
        }

        public Builder footer(View footer){
            mFooter = footer;
            return this;
        }

        public Builder minFooterTranslation(int minFooterTranslation){
            mMinFooterTranslation = minFooterTranslation;
            return this;
        }

        public Builder listener(CollapseListener listener){
            mListener = listener;
            return this;
        }

        public Builder columnCount(int columnCount){
            mColumnCount = columnCount;
            return this;
        }

        public Builder isSnappable(boolean isSnappable){
            mIsSnappable = isSnappable;
            return this;
        }

        public Builder isGrid(boolean isGrid){
            this.isGrid = isGrid;
            return this;
        }

        public Builder adapter(RemindersRecyclerAdapter adapter){
            this.adapter = adapter;
            return this;
        }

        public QuickReturnRecyclerViewOnScrollListener build() {
            return new QuickReturnRecyclerViewOnScrollListener(this);
        }
    }
}
