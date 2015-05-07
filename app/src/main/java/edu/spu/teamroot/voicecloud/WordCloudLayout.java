package edu.spu.teamroot.voicecloud;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;


public class WordCloudLayout extends RelativeLayout {
    private static final float MIN_SCALE = 0.1f;
    private static final float MAX_SCALE = 1.0f;
    private float mScaleFactor = 1.0f;

    private ScaleGestureDetector mScaleGestureDetector;

    private final ScaleGestureDetector.OnScaleGestureListener mScaleGestureListener = new ScaleGestureDetector.SimpleOnScaleGestureListener() {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            // Get the current percentage by which to scale
            float scaleFactor = detector.getScaleFactor();

            mScaleFactor *= scaleFactor;
            mScaleFactor = Math.max(MIN_SCALE, Math.min(mScaleFactor, MAX_SCALE));

            invalidate();
            return true;
        }
    };

    private void initScaleDetector(Context context) {
        mScaleGestureDetector = new ScaleGestureDetector(context, mScaleGestureListener);
    }

    public WordCloudLayout(Context context) {
        super(context);
        initScaleDetector(context);
    }

    public WordCloudLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        initScaleDetector(context);
    }

    public WordCloudLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initScaleDetector(context);
    }

    protected void dispatchDraw(Canvas canvas) {
        canvas.save();
        canvas.scale(mScaleFactor, mScaleFactor);
        super.dispatchDraw(canvas);
        canvas.restore();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean retval = false;
        retval |= mScaleGestureDetector.onTouchEvent(event);
        retval |= super.onTouchEvent(event);
        return retval;
    }
}
