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

    protected Rect bounds;

    protected WordGroup parent;
    protected ArrayList<WordGroup> children;

    public WordGroup() {
        precision = Precision.FAVOR_EXISTING;
        bounds = new Rect();
        parent = null;
        children = new ArrayList<>();
    }

    // Moves the group and all its children by a specified delta.
    public void moveBy(int dx, int dy) {
        bounds.offset(dx, dy);

        // Move all children
        for (WordGroup child : children) {
            child.moveBy(dx, dy);
        }
    }

    // Moves the group and all its children to a specified point.
    public void moveTo(int x, int y) {
        int dx = x - bounds.left;
        int dy = y - bounds.top;

        moveBy(dx, dy);
    }

    public Rect getBounds() {
        return new Rect(bounds); // Return a copy of bounds
    }

    public void addChild(WordGroup child) {
        children.add(child);
        child.parent = this;

        bounds.union(child.bounds);
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

        // TODO: The center will move around... idk if we can get a fixed center AND bounds
        // TODO: We might be able to keep track of center and move it along with the bounds
        int groupCenterX = bounds.centerX();
        int groupCenterY = bounds.centerY();

        int x, y;

        do {
            double radius = Math.pow(position + 1, 0.1);
            angle += Math.asin(0.1 / radius);

            x = (int)(Math.cos(angle) * (radius * position)) + groupCenterX;
            y = (int)(Math.sin(angle) * (radius * position)) + groupCenterY;

            position += 0.1;
        } while (isChildOverlap(child, x, y));

        child.moveTo(x, y);

        // Update bounds
        bounds.setEmpty();

        for (WordGroup group : children) {
            bounds.union(group.bounds);
        }
    }

    // Checks if a group is overlapping any other groups at the specified location.
    private boolean isChildOverlap(WordGroup group, int x, int y) {
        int dx = x - group.bounds.left;
        int dy = y - group.bounds.top;

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
