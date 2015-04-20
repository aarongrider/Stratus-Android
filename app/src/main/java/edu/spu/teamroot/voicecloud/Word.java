package edu.spu.teamroot.voicecloud;

import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;

public class Word extends WordGroup {
    private String name;
    private int count;

    public Button wordObject;

    public Word(String name, int count, Button button) {
        super();

        this.name = name;
        this.count = count;
        this.wordObject = button;

        refreshSize(false);
    }

    public String getName() {
        return name;
    }

    public int getCount() {
        return count;
    }

    public void incrementCount() {
        if (count < Integer.MAX_VALUE) {
            count++;
            refreshSize(true);
        }
    }

    public void incrementCount(int value) {
        if (count + value <= Integer.MAX_VALUE) {
            count += value;
            refreshSize(true);
        }
    }

    private void refreshSize(boolean animate) {
        // Calculate new size based on count, and animate the word growing (or showing up for the first time)
        // Possibly change the color too
        // TODO
    }

    public void moveBy(int dx, int dy, boolean animate) {
        moveTo(bounds.left + dx, bounds.top + dy, animate);
    }

    @Override
    public void moveTo(int x, int y) {
        // Only animate if the button is visible, otherwise, just move it
        moveTo(x, y, wordObject.getVisibility() == View.VISIBLE);
    }

    public void moveTo(int x, int y, boolean animate) {
        super.moveTo(x, y);
        // In addition, move the actual word button! (& animate it)
        // Also update bounds
        // TODO
    }
}
