package edu.spu.teamroot.voicecloud;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.util.Log;
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

        button = createButton(name);

        // Add the button to the word cloud layout
        layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.topMargin = 0;
        layoutParams.leftMargin = 0;
        WordCloud.layout.addView(button, layoutParams);

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

    public void show() {
        show(button.getVisibility() == View.INVISIBLE);
    }

    public void show(boolean animate) {
        button.setVisibility(View.VISIBLE);

        if (animate) {
            ScaleAnimation anim = new ScaleAnimation(0.0f, 1.0f, 0.0f, 1.0f, ScaleAnimation.RELATIVE_TO_SELF, 0.5f, ScaleAnimation.RELATIVE_TO_SELF, 0.5f);
            anim.setInterpolator(new SpringInterpolator());

            anim.setDuration(1000);

            button.clearAnimation();
            button.setAnimation(anim);
            button.animate();
        }
    }

    public void hide() {
        hide(button.getVisibility() == View.VISIBLE);
    }

    public void hide(boolean animate) {
        button.setVisibility(View.INVISIBLE);

        if (animate) {
            // TODO: Animate with fade-out
        }
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
        Log.d(name, "Count: " + count + " Size: " + getTextSize(count));

        // TODO: Calculate new color based on count (or part of speech)

        if (button.getWidth() == 0 && button.getHeight() == 0) {
            // The button has likely not been drawn in the View yet...
            // We need to fallback and get a calculated size based on the layout
            button.measure(WordCloud.layout.getWidth(), WordCloud.layout.getHeight());

            bounds.set(bounds.left, bounds.top,
                    bounds.left + UnitConverter.getInstance().toDp(button.getMeasuredWidth()),
                    bounds.right + UnitConverter.getInstance().toDp(button.getMeasuredHeight()));
        } else {
            bounds.set(bounds.left, bounds.top,
                    bounds.left + UnitConverter.getInstance().toDp(button.getWidth()),
                    bounds.right + UnitConverter.getInstance().toDp(button.getHeight()));
        }

        Log.d(name, "Bounds: " + bounds.toString());

        center.x = bounds.centerX();
        center.y = bounds.centerY();

        Log.d(name, "Center: " + center.toString());

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
        center.offset(dx, dy);
        bounds.offset(dx, dy);

        layoutParams.leftMargin = UnitConverter.getInstance().toPx(bounds.left);
        layoutParams.topMargin = UnitConverter.getInstance().toPx(bounds.top);

        Log.d(name, "moveBy: (" + dx + "," + dy + ") Bounds: " + bounds.toString());
        Log.d(name, "moveBy: (" + dx + "," + dy + ") Center: " + center.toString());

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
        int dx = x - center.x;
        int dy = y - center.y;

        moveBy(dx, dy, animate);
    }
}
