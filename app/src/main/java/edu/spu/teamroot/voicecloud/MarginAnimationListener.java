package edu.spu.teamroot.voicecloud;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;

public class MarginAnimationListener implements AnimatorUpdateListener, AnimatorListener {
    public static final int LEFT_MARGIN = 0;
    public static final int TOP_MARGIN = 1;
    public static final int RIGHT_MARGIN = 2;
    public static final int BOTTOM_MARGIN = 3;

    private ViewGroup mLayout;
    private MarginLayoutParams mParams;
    private int mDirection;
    private int mFromValue;
    private int mToValue;

    private boolean mCancelled = false;

    public MarginAnimationListener(ViewGroup layout, MarginLayoutParams params, int direction, int toValue) {
        mLayout = layout;
        mParams = params;
        mDirection = direction;

        switch (mDirection) {
            default:
            case LEFT_MARGIN:
                mFromValue = mParams.leftMargin;
                break;
            case TOP_MARGIN:
                mFromValue = mParams.topMargin;
                break;
            case RIGHT_MARGIN:
                mFromValue = mParams.rightMargin;
                break;
            case BOTTOM_MARGIN:
                mFromValue = mParams.bottomMargin;
                break;
        }

        mToValue = toValue;
    }

    public MarginAnimationListener(ViewGroup layout, MarginLayoutParams params, int direction, int fromValue, int toValue) {
        mLayout = layout;
        mParams = params;
        mDirection = direction;
        mFromValue = fromValue;
        mToValue = toValue;
    }

    // Returns a ValueAnimator configured to use this MarginAnimationListener.
    // Use this animator to animate the margins of views.
    public ValueAnimator getValueAnimator() {
        ValueAnimator animator = ValueAnimator.ofInt(0, mToValue - mFromValue);
        animator.addUpdateListener(this);
        animator.addListener(this);

        return animator;
    }

    // Sets the value of a margin based on the animation direction.
    private void setValue(int value) {
        switch (mDirection) {
            default:
            case LEFT_MARGIN:
                mParams.leftMargin = value;
                break;
            case TOP_MARGIN:
                mParams.topMargin = value;
                break;
            case RIGHT_MARGIN:
                mParams.rightMargin = value;
                break;
            case BOTTOM_MARGIN:
                mParams.bottomMargin = value;
                break;
        }

        mLayout.requestLayout();
    }

    @Override
    public void onAnimationUpdate(ValueAnimator animation) {
        int delta = (int)animation.getAnimatedValue();
        setValue(mFromValue + delta);
        //Log.d("MarginAnimationListener", "From: " + mFromValue + " Delta: " + delta);
    }

    @Override
    public void onAnimationStart(Animator animation) {
        mCancelled = false;
        setValue(mFromValue);
    }

    @Override
    public void onAnimationEnd(Animator animation) {
        // Do not set the final value if cancelled
        if (!mCancelled) {
            setValue(mToValue);
        }
    }

    @Override
    public void onAnimationCancel(Animator animation) {
        // Stop animation in its tracks
        mCancelled = true;
    }

    @Override
    public void onAnimationRepeat(Animator animation) {
        setValue(mFromValue);
    }
}
