package edu.spu.teamroot.voicecloud;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.HorizontalScrollView;
import android.widget.ScrollView;

/**
 * Created by aarongrider on 2/9/15.
 */

public class WordCloudScrollView extends HorizontalScrollView {

    public WordCloudScrollView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public WordCloudScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public WordCloudScrollView(Context context) {
        super(context);
    }

    //@Override
    //public boolean onTouchEvent(MotionEvent ev) { return false; }
}