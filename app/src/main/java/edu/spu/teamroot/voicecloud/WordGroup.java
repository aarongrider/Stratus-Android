package edu.spu.teamroot.voicecloud;

import android.graphics.Point;
import android.graphics.Rect;
import android.view.View;

import java.util.ArrayList;

public class WordGroup {
    private static enum Precision {
        COARSE,
        FAVOR_EXISTING,
        FAVOR_CURRENT,
        FINE_GRAINED
    }

    private Precision precision;

    protected Point center;
    protected Rect bounds;

    protected WordGroup parent;
    protected ArrayList<WordGroup> children;

    public WordGroup() {
        precision = Precision.COARSE;
        center = new Point();
        bounds = new Rect();
        parent = null;
        children = new ArrayList<>();
    }

    // Moves the group and all its children by a specified delta.
    public void moveBy(int dx, int dy) {
        center.offset(dx, dy);
        bounds.offset(dx, dy);

        // Move all children
        for (WordGroup child : children) {
            child.moveBy(dx, dy);
        }
    }

    // Moves the group and all its children to a specified point.
    public void moveTo(int x, int y) {
        int dx = x - center.x; // TODO: This used to be offsetting the bounds' top-left corner
        int dy = y - center.y;

        moveBy(dx, dy);
    }

    // Sets the initial bounds of the group. This should only be called once.
    public void setBounds(Rect rect) {
        bounds = new Rect(rect);
        center.x = bounds.centerX();
        center.y = bounds.centerY();
    }

    // Returns a copy of the group's bounds.
    public Rect getBounds() {
        return new Rect(bounds);
    }

    // Returns a copy of the group's center point.
    // (Note: this is not the same as the center of the group's bounds)
    public Point getCenter() {
        return new Point(center);
    }

    public void addChild(WordGroup child) {
        children.add(child);
        child.parent = this;
    }

    public boolean removeChild(WordGroup child) {
        int index = children.indexOf(child);

        if (index >= 0) {
            children.remove(index);
            child.parent = null;
            return true;
        }

        return false;
    }

    public void repositionChild(WordGroup child) {
        // TODO: Investigate other spiral formula, and use it instead

        double position = 0;
        double angle = 0;

        //int groupCenterX = bounds.centerX();
        //int groupCenterY = bounds.centerY();

        int x, y;

        do {
            double radius = Math.pow(position + 1, 0.1);
            angle += Math.asin(1 / radius);

            x = (int)(Math.cos(angle) * (radius * position)) + center.x;
            y = (int)(Math.sin(angle) * (radius * position)) + center.y;

            position += 1;
        } while (isChildOverlap(child, x, y));

        child.moveTo(x, y);

        // Update bounds (do not update if root)
        if (parent != null) {
            bounds.setEmpty();

            for (WordGroup group : children) {
                bounds.union(group.bounds);
            }
        }
    }

    // Checks if a group is overlapping any other groups at the specified location.
    private boolean isChildOverlap(WordGroup group, int x, int y) {
        int dx = x - group.center.x; // TODO: Used to be bounds
        int dy = y - group.center.y;

        // Get group's potential new position
        Rect checkBounds = group.getBounds();
        checkBounds.offset(dx, dy);

        // Check all children
        for (WordGroup child : children) {
            if (child.equals(group)) {
                continue; // Skip if checking against itself
            }

            // Check intersection
            switch (precision) {
                default:
                case COARSE:
                    if (Rect.intersects(checkBounds, child.bounds)) {
                        return true;
                    }
                    break;
                case FAVOR_EXISTING:
                    for (WordGroup grandChild : child.children) {
                        if (Rect.intersects(checkBounds, grandChild.bounds)) {
                            return true;
                        }
                    }
                    break;
                case FAVOR_CURRENT:
                    for (WordGroup groupChild : group.children) {
                        // Get child's potential new position
                        Rect checkChild = groupChild.getBounds();
                        checkChild.offset(dx, dy);

                        if (Rect.intersects(checkChild, child.bounds)) {
                            return true;
                        }
                    }
                    break;
                case FINE_GRAINED:
                    for (WordGroup grandChild : child.children) {
                        for (WordGroup groupChild : group.children) {
                            // Get child's potential new position
                            Rect checkChild = groupChild.getBounds();
                            checkChild.offset(dx, dy);

                            if (Rect.intersects(checkChild, grandChild.bounds)) {
                                return true;
                            }
                        }
                    }
                    break;
            }
        }

        return false;
    }
}
