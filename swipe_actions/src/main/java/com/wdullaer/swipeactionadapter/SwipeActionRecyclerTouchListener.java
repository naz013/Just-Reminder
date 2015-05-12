/*
* Copyright 2013 Google Inc
* Copyright 2014 Wouter Dullaert
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package com.wdullaer.swipeactionadapter;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.graphics.Rect;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SwipeActionRecyclerTouchListener implements View.OnTouchListener {
    // Cached ViewConfiguration and system-wide constant values
    private int mSlop;
    private int mMinFlingVelocity;
    private int mMaxFlingVelocity;
    private long mAnimationTime;

    // Fixed properties
    private RecyclerView mRecycler;
    private ActionCallbacks mCallbacks;
    private int mViewWidth = 1; // 1 and not 0 to prevent dividing by zero
    private boolean mFadeOut = false;
    private boolean mFixedBackgrounds = false;
    private float mFarSwipeFraction = 0.5f;

    // Transient properties
    private List<PendingDismissData> mPendingDismisses = new ArrayList<>();
    private int mDismissAnimationRefCount = 0;
    private float mDownX;
    private float mDownY;
    private boolean mSwiping;
    private int mSwipingSlop;
    private VelocityTracker mVelocityTracker;
    private int mDownPosition;
    private View mDownView;
    private SwipeViewGroup mDownViewGroup;
    private boolean mPaused;
    private int mDirection;
    private boolean mFar;

    public interface ActionCallbacks {
        boolean hasActions(int position);

        boolean onPreAction(RecyclerView listView, int position, int direction);

        void onAction(RecyclerView listView, int[] position, int[] direction);
    }

    public SwipeActionRecyclerTouchListener(RecyclerView listView, ActionCallbacks callbacks) {
        ViewConfiguration vc = ViewConfiguration.get(listView.getContext());
        mSlop = vc.getScaledTouchSlop();
        mMinFlingVelocity = vc.getScaledMinimumFlingVelocity() * 16;
        mMaxFlingVelocity = vc.getScaledMaximumFlingVelocity();
        mAnimationTime = listView.getContext().getResources().getInteger(
                android.R.integer.config_shortAnimTime);
        mRecycler = listView;
        mCallbacks = callbacks;
    }

    public void setEnabled(boolean enabled) {
        mPaused = !enabled;
    }

    public RecyclerView.OnScrollListener makeRecyclerScrollListener() {
        return new RecyclerView.OnScrollListener() {

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            }

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                setEnabled(newState != RecyclerView.SCROLL_STATE_DRAGGING);
            }
        };
    }

    protected void setFadeOut(boolean fadeOut){
        mFadeOut = fadeOut;
    }

    protected void setFixedBackgrounds(boolean fixedBackgrounds){
        mFixedBackgrounds = fixedBackgrounds;
    }

    protected void setFarSwipeFraction(float farSwipeFraction) {
        mFarSwipeFraction = farSwipeFraction;
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        if (mViewWidth < 2) {
            mRecycler.getWidth();
        }

        switch (motionEvent.getActionMasked()) {
            case MotionEvent.ACTION_DOWN: {
                if (mPaused) {
                    return false;
                }

                // TODO: ensure this is a finger, and set a flag

                // Find the child view that was touched (perform a hit test)
                Rect rect = new Rect();
                int childCount = mRecycler.getLayoutManager().getChildCount();
                int[] listViewCoords = new int[2];
                mRecycler.getLocationOnScreen(listViewCoords);
                int x = (int) motionEvent.getRawX() - listViewCoords[0];
                int y = (int) motionEvent.getRawY() - listViewCoords[1];
                View child;
                for (int i = 0; i < childCount; i++) {
                    child = mRecycler.getLayoutManager().getChildAt(i);
                    child.getHitRect(rect);
                    if (rect.contains(x, y)) {
                        try {
                            mDownViewGroup = (SwipeViewGroup) child;
                            mDownView = mFixedBackgrounds ? mDownViewGroup.getContentView() : child;
                            if(!mFixedBackgrounds) mDownViewGroup.translateBackgrounds();
                        }
                        catch(Exception e) {
                            mDownView = child;
                        }
                        break;
                    }
                }

                if (mDownView != null) {
                    mDownX = motionEvent.getRawX();
                    mDownY = motionEvent.getRawY();
                    mDownPosition = mRecycler.getLayoutManager().getPosition(mDownView);
                    if (mCallbacks.hasActions(mDownPosition)) {
                        mVelocityTracker = VelocityTracker.obtain();
                        mVelocityTracker.addMovement(motionEvent);
                    } else {
                        mDownView = null;
                    }
                }
                return false;
            }

            case MotionEvent.ACTION_CANCEL: {
                if (mVelocityTracker == null) {
                    break;
                }

                if (mDownView != null && mSwiping) {
                    // cancel
                    mDownView.animate()
                            .translationX(0)
                            .alpha(1)
                            .setDuration(mAnimationTime)
                            .setListener(null);
                }
                mVelocityTracker.recycle();
                mVelocityTracker = null;
                mDownX = 0;
                mDownY = 0;
                mDownView = null;
                mDownPosition = RecyclerView.NO_POSITION;
                mSwiping = false;
                mDirection = SwipeDirections.DIRECTION_NEUTRAL;
                mFar = false;
                break;
            }

            case MotionEvent.ACTION_UP: {
                if (mVelocityTracker == null) {
                    break;
                }

                float deltaX = motionEvent.getRawX() - mDownX;
                mVelocityTracker.addMovement(motionEvent);
                mVelocityTracker.computeCurrentVelocity(1000);
                float velocityX = mVelocityTracker.getXVelocity();
                float absVelocityX = Math.abs(velocityX);
                float absVelocityY = Math.abs(mVelocityTracker.getYVelocity());
                boolean dismiss = false;
                boolean dismissRight = false;
                if (Math.abs(deltaX) > mViewWidth / 2 && mSwiping) {
                    dismiss = true;
                    dismissRight = deltaX > 0;
                } else if (mMinFlingVelocity <= absVelocityX && absVelocityX <= mMaxFlingVelocity
                        && absVelocityY < absVelocityX && mSwiping) {
                    // dismiss only if flinging in the same direction as dragging
                    dismiss = (velocityX < 0) == (deltaX < 0);
                    dismissRight = mVelocityTracker.getXVelocity() > 0;
                }
                if (dismiss && mDownPosition != RecyclerView.NO_POSITION) {
                    // dismiss
                    final View downView = mDownView; // mDownView gets null'd before animation ends
                    final int downPosition = mDownPosition;
                    final int direction = mDirection;
                    ++mDismissAnimationRefCount;
                    mDownView.animate()
                            .translationX(dismissRight ? mViewWidth : -mViewWidth)
                            .alpha(mFadeOut ? 0 : 1)
                            .setDuration(mAnimationTime)
                            .setListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    boolean performDismiss = mCallbacks.onPreAction(
                                                mRecycler,
                                                downPosition,
                                                direction
                                        );
                                    if(performDismiss) performDismiss(downView,downPosition,direction);
                                    else slideBack(downView, downPosition, direction);
                                }
                            });
                } else {
                    // cancel
                    mDownView.animate()
                            .translationX(0)
                            .alpha(1)
                            .setDuration(mAnimationTime)
                            .setListener(null);
                }
                mVelocityTracker.recycle();
                mVelocityTracker = null;
                mDownX = 0;
                mDownY = 0;
                mDownView = null;
                mDownPosition = RecyclerView.NO_POSITION;
                mSwiping = false;
                mDirection = SwipeDirections.DIRECTION_NEUTRAL;
                mFar = false;
                break;
            }

            case MotionEvent.ACTION_MOVE: {
                if (mVelocityTracker == null || mPaused) {
                    break;
                }

                mVelocityTracker.addMovement(motionEvent);
                float deltaX = motionEvent.getRawX() - mDownX;
                float deltaY = motionEvent.getRawY() - mDownY;
                if (!mSwiping && Math.abs(deltaX) > mSlop && Math.abs(deltaY) < Math.abs(deltaX) / 2) {
                    mSwiping = true;
                    mSwipingSlop = (deltaX > 0 ? mSlop : -mSlop);

                    mRecycler.requestDisallowInterceptTouchEvent(true);

                    // Cancel ListView's touch (un-highlighting the item)
                    MotionEvent cancelEvent = MotionEvent.obtain(motionEvent);
                    cancelEvent.setAction(MotionEvent.ACTION_CANCEL |
                            (motionEvent.getActionIndex()
                                    << MotionEvent.ACTION_POINTER_INDEX_SHIFT));
                    mRecycler.onTouchEvent(cancelEvent);
                    cancelEvent.recycle();
                }

                if (mSwiping) {
                    if(mDirection*deltaX < 0) mFar = false;
                    if(!mFar && Math.abs(deltaX) > mViewWidth*mFarSwipeFraction) mFar = true;
                    if(!mFar) mDirection = (deltaX > 0 ? SwipeDirections.DIRECTION_NORMAL_RIGHT : SwipeDirections.DIRECTION_NORMAL_LEFT);
                    else mDirection = (deltaX > 0 ? SwipeDirections.DIRECTION_FAR_RIGHT : SwipeDirections.DIRECTION_FAR_LEFT);
                    mDownViewGroup.showBackground(mDirection);

                    mDownView.setTranslationX(deltaX - mSwipingSlop);
                    if(mFadeOut) mDownView.setAlpha(Math.max(0f, Math.min(1f,
                                1f - 2f * Math.abs(deltaX) / mViewWidth)));
                    return true;
                }
                break;
            }
        }
        return false;
    }

    class PendingDismissData implements Comparable<PendingDismissData> {
        public int position;
        public int direction;
        public View view;

        public PendingDismissData(int position, int direction, View view) {
            this.position = position;
            this.direction = direction;
            this.view = view;
        }

        @Override
        public int compareTo(@NonNull PendingDismissData other) {
            // Sort by descending position
            return other.position - position;
        }
    }

    private void slideBack(final View slideInView, final int downPosition, final int direction){
        mPendingDismisses.add(new PendingDismissData(downPosition,direction,slideInView));
        slideInView.setTranslationX(slideInView.getTranslationX());
        slideInView.animate()
                .translationX(0)
                .alpha(1)
                .setDuration(mAnimationTime)
                .setListener(createAnimatorListener(slideInView.getHeight()));
    }

    private void performDismiss(final View dismissView, final int dismissPosition, final int direction) {
        // Animate the dismissed list item to zero-height and fire the dismiss callback when
        // all dismissed list item animations have completed. This triggers layout on each animation
        // frame; in the future we may want to do something smarter and more performant.

        final ViewGroup.LayoutParams lp = dismissView.getLayoutParams();
        final int originalHeight = dismissView.getHeight();

        ValueAnimator animator = ValueAnimator.ofInt(originalHeight, 1).setDuration(mAnimationTime);

        animator.addListener(createAnimatorListener(originalHeight));

        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                lp.height = (Integer) valueAnimator.getAnimatedValue();
                dismissView.setLayoutParams(lp);
            }
        });

        mPendingDismisses.add(new PendingDismissData(dismissPosition, direction, dismissView));
        animator.start();
    }

    private AnimatorListenerAdapter createAnimatorListener(final int originalHeight){
        return new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                --mDismissAnimationRefCount;
                if (mDismissAnimationRefCount == 0) {
                    // No active animations, process all pending dismisses.
                    // Sort by descending position
                    Collections.sort(mPendingDismisses);

                    int[] dismissPositions = new int[mPendingDismisses.size()];
                    int[] dismissDirections = new int [mPendingDismisses.size()];
                    for (int i = mPendingDismisses.size() - 1; i >= 0; i--) {
                        dismissPositions[i] = mPendingDismisses.get(i).position;
                        dismissDirections[i] = mPendingDismisses.get(i).direction;
                    }
                    mCallbacks.onAction(mRecycler, dismissPositions, dismissDirections);

                    // Reset mDownPosition to avoid MotionEvent.ACTION_UP trying to start a dismiss
                    // animation with a stale position
                    mDownPosition = RecyclerView.NO_POSITION;

                    ViewGroup.LayoutParams lp;
                    for (PendingDismissData pendingDismiss : mPendingDismisses) {
                        // Reset view presentation
                        pendingDismiss.view.setAlpha(1f);
                        pendingDismiss.view.setTranslationX(0);
                        lp = pendingDismiss.view.getLayoutParams();
                        lp.height = originalHeight;
                        pendingDismiss.view.setLayoutParams(lp);
                    }

                    // Send a cancel event
                    long time = SystemClock.uptimeMillis();
                    MotionEvent cancelEvent = MotionEvent.obtain(time, time,
                            MotionEvent.ACTION_CANCEL, 0, 0, 0);
                    mRecycler.dispatchTouchEvent(cancelEvent);

                    mPendingDismisses.clear();
                }
            }
        };
    }
}