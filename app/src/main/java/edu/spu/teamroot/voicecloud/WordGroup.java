package edu.spu.teamroot.voicecloud;

import android.graphics.Point;
import android.graphics.Rect;
import android.view.View;

import java.util.ArrayList;

public class WordGroup {
    protected Rect bounds;

    public WordGroup parent;
    public ArrayList<WordGroup> children;

    public WordGroup() {

    }

    public void moveBy(int dx, int dy) {
        moveTo(bounds.left + dx, bounds.top + dy);
    }

    public void moveTo(int x, int y) {
        bounds.offsetTo(x, y);
    }

    public Rect getBounds() {
        return new Rect(bounds); // Return a copy of bounds
    }

    public void addChild(WordGroup child) {

    }

    public void repositionChild(WordGroup child) {

    }
}
