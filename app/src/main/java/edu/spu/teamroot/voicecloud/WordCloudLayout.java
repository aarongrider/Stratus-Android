package edu.spu.teamroot.voicecloud;

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

    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);

        if (WordCloud.getInstance() != null) {
            WordCloud.getInstance().onDraw(canvas);
        }
    }

    public void drawToCanvas(Canvas canvas) {
        super.draw(canvas);
        //dispatchDraw(canvas);
    }
}
