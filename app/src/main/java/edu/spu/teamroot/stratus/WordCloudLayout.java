package edu.spu.teamroot.stratus;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

public class WordCloudLayout extends RelativeLayout {
    public WordCloudLayout(Context context) {
        super(context);
    }

    public WordCloudLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public WordCloudLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    // Dispatches a draw to its children. Enables drawing of word group outlines.
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);

        if (WordCloud.getInstance() != null) {
            WordCloud.getInstance().onDraw(canvas);
        }
    }

    // Draws the layout to a canvas object.
    public void drawToCanvas(Canvas canvas) {
        super.draw(canvas);
        //dispatchDraw(canvas);
    }
}
