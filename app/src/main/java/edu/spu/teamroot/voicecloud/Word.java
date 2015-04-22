package edu.spu.teamroot.voicecloud;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.RelativeLayout;

public class Word extends WordGroup {
    private String name;
    private int count;

    public Button button;
    public RelativeLayout.LayoutParams layoutParams;

    public Word(String name, int count) {
        super();

        this.name = name;
        this.count = count;
        this.button = createButton(name);

        // Add the button to the word cloud layout
        layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.topMargin = 0;
        layoutParams.leftMargin = 0;
        WordCloud.layout.addView(this.button, layoutParams);

        refreshSize(false);
    }

    private Button createButton(String text) {
        Button button = new Button(WordCloud.context);
        button.setVisibility(View.INVISIBLE);

        button.setText(text);
        button.setTextColor(WordCloud.context.getResources().getColor(android.R.color.white));

        button.getBackground().setColorFilter(Color.parseColor("#20A760"), PorterDuff.Mode.SRC_ATOP);

        return button;
    }

    // Removes the word from the tree and deletes the associated button.
    public void delete() {
        if (parent != null) {
            parent.removeChild(this);
        }

        WordCloud.layout.removeView(button);
        button = null;
        layoutParams = null;
    }

    public String getName() {
        return name;
    }

    public int getCount() {
        return count;
    }

    public void incrementCount() {
        count++;
        refreshSize(true);
    }

    public void incrementCount(int value) {
        count += value;
        refreshSize(true);
    }

    private void refreshSize(boolean animate) {
        // Cache old size
        Rect oldBounds = new Rect(bounds);

        // Calculate new size based on count
        button.setTextSize(getTextSize(count));

        bounds.set(bounds.left, bounds.top,
                bounds.left + UnitConverter.getInstance().toDp(button.getWidth()),
                bounds.right + UnitConverter.getInstance().toDp(button.getHeight()));

        if (animate) {
            float prevX = (float)oldBounds.width() / bounds.width();
            float prevY = (float)oldBounds.height() / bounds.height();

            ScaleAnimation anim = new ScaleAnimation(prevX, 1.0f, prevY, 1.0f);
            anim.setInterpolator(new SpringInterpolator());
            anim.setDuration(500);
            anim.setStartOffset(100);

            button.clearAnimation();
            button.setAnimation(anim);
            button.animate();
        }
    }

    private int getTextSize(int count) {
        return count * 2; // TODO: Update sizing algorithm
    }

    @Override
    public void moveBy(int dx, int dy) {
        // Only animate if the button is visible, otherwise, just move it
        moveBy(dx, dy, button.getVisibility() == View.VISIBLE);
    }

    private void moveBy(int dx, int dy, boolean animate) {
        bounds.offset(dx, dy);

        layoutParams.leftMargin = UnitConverter.getInstance().toPx(bounds.left);
        layoutParams.topMargin = UnitConverter.getInstance().toPx(bounds.top);

        if (animate) {
            // TODO: Animate movement
        }
    }

    @Override
    public void moveTo(int x, int y) {
        // Only animate if the button is visible, otherwise, just move it
        moveTo(x, y, button.getVisibility() == View.VISIBLE);
    }

    private void moveTo(int x, int y, boolean animate) {
        bounds.offsetTo(x, y);

        layoutParams.leftMargin = UnitConverter.getInstance().toPx(bounds.left);
        layoutParams.topMargin = UnitConverter.getInstance().toPx(bounds.top);

        if (animate) {
            // TODO: Animate movement
        }
    }
}
