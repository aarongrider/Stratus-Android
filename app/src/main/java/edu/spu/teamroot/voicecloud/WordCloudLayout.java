package edu.spu.teamroot.voicecloud;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.ScaleGestureDetector;
import android.widget.RelativeLayout;

/**
 * Created by Wang Laptop on 3/2/2015.
 */
public class WordCloudLayout extends RelativeLayout {
    private static float MIN_ZOOM = 1f;
    private static float MAX_ZOOM = 5f;

    private float scaleFactor = 1f;

    private ScaleGestureDetector scaleDetector;

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            scaleFactor *= detector.getScaleFactor();
            scaleFactor = Math.max(MIN_ZOOM, Math.min(scaleFactor, MAX_ZOOM));
            invalidate();
            return true;
        }
    }

    private void initScaleDetector(Context context) {
        scaleDetector = new ScaleGestureDetector(context, new ScaleListener());
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
        canvas.scale(scaleFactor, scaleFactor);
        canvas.restore();
    }
}
