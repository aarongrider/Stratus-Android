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
        bounds = new Rect();
        children = new ArrayList<>();
    }

    public void moveBy(int dx, int dy) {
        moveTo(bounds.left + dx, bounds.top + dy);
    }

    public void moveTo(int x, int y) {
        bounds.offsetTo(x, y);
        // TODO -- Move children too!
    }

    public Rect getBounds() {
        return new Rect(bounds); // Return a copy of bounds
    }

    public void addChild(WordGroup child) {
        children.add(child);
        child.parent = this;
        // TODO -- is there more to do, like updating bounds?
    }

    public void repositionChild(WordGroup child) {
        // TODO -- do spiral positioning algorithm; also update bounds?
    }
}
