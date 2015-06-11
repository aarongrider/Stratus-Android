package edu.spu.teamroot.speechcloud;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.Resources.Theme;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import android.view.animation.Interpolator;

/**
 * An interpolator where the change overshoots the last value, then
 * oscillates around the last value until gradually coming to rest.
 */
public class SpringInterpolator implements Interpolator {
    private static final float DEFAULT_CYCLES = 2.25f;
    private static final float DEFAULT_DAMPING = 3.0f;

    private final float mCycles;
    private final float mDamping;

    public SpringInterpolator() {
        mCycles = DEFAULT_CYCLES;
        mDamping = -1.0f * mCycles * DEFAULT_DAMPING;
    }

    /**
     * @param damping Amount of damping. When damping equals 0.0f, there is
     *                no damping and the interpolator becomes a simple
     *                CycleInterpolator.
     */
    public SpringInterpolator(float damping) {
        mCycles = DEFAULT_CYCLES;
        mDamping = -1.0f * mCycles * damping;
    }

    /**
     * @param damping Amount of damping. When damping equals 1.0f, there is
     *                no damping and the interpolator becomes a simple
     *                CycleInterpolator.
     *
     * @param cycles  Number of cycles. The animation will overshoot and
     *                undershoot for this number of times.
     */
    public SpringInterpolator(float damping, float cycles) {
        mCycles = cycles;
        mDamping = -1.0f * mCycles * damping;
    }

    public SpringInterpolator(Context context, AttributeSet attrs) {
        this(context.getResources(), context.getTheme(), attrs);
    }

    /** @hide */
    public SpringInterpolator(Resources res, Theme theme, AttributeSet attrs) {
        TypedArray a;
        if (theme != null) {
            a = theme.obtainStyledAttributes(attrs, R.styleable.SpringInterpolator, 0, 0);
        } else {
            a = res.obtainAttributes(attrs, R.styleable.SpringInterpolator);
        }

        mCycles = a.getFloat(R.styleable.SpringInterpolator_cycles, DEFAULT_CYCLES);
        mDamping = -1.0f * mCycles * a.getFloat(R.styleable.SpringInterpolator_damping, DEFAULT_DAMPING);
    }

    /** @hide */
    @Override
    public float getInterpolation(float t) {
        // 1 - e^((-damping) * t) cos(spring * t)
        return (float)(1.0f - (Math.exp(mDamping * t) * Math.cos(2 * Math.PI * mCycles * t)));
    }
}
