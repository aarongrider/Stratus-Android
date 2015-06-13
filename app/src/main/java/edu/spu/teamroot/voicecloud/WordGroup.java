package edu.spu.teamroot.voicecloud;

import android.graphics.Point;
import android.graphics.Rect;

import java.util.ArrayList;
import java.util.Random;

public class WordGroup {
    private static int curIndex = 0;

    private enum Precision {
        COARSE,
        FAVOR_EXISTING,
        FAVOR_CURRENT,
        FINE_GRAINED
    }

    private final int GROUP_ID;

    private Precision precision;

    protected Point center;
    protected Rect bounds;

    protected WordGroup parent;
    protected ArrayList<WordGroup> children;

    public WordGroup() {
        if (this.getClass() != WordGroup.class) {
            // This is not a direct instance of a WordGroup
            GROUP_ID = -1;
        } else {
            GROUP_ID = curIndex++;
        }

        precision = Precision.COARSE;
        center = new Point();
        bounds = new Rect();
        parent = null;
        children = new ArrayList<>();
    }

    public WordGroup(Point center, Rect bounds) {
        if (this.getClass() != WordGroup.class) {
            // This is not a direct instance of a WordGroup
            GROUP_ID = -1;
        } else {
            GROUP_ID = curIndex++;
        }

        precision = Precision.COARSE;
        this.center = center;
        this.bounds = bounds;
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
        int dx = x - center.x;
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

    // Refreshes the group's bounds.
    public void refreshBounds() {
        // Refresh group bounds
        bounds.setEmpty();

        for (WordGroup child : children) {
            bounds.union(child.bounds);
        }

        // Update center
        // TODO Test changing the center on bounds update
        center.x = bounds.centerX();
        center.y = bounds.centerY();
    }

    // Returns a copy of the group's center point.
    // (Note: this is not the same as the center of the group's bounds)
    public Point getCenter() {
        return new Point(center);
    }

    // Returns the ID of this group. Needed to identify groups when saving.
    public int getId() {
        return GROUP_ID;
    }

    // Adds a child to the group.
    public void addChild(WordGroup child) {
        children.add(child);
        child.parent = this;
    }

    // Removes a child from the group.
    public boolean removeChild(WordGroup child) {
        int index = children.indexOf(child);

        if (index >= 0) {
            children.remove(index);
            child.parent = null;
            return true;
        }

        return false;
    }

    // Repositions a child in the group with respect to the other children.
    // Uses a spiral algorithm to position children; ensures that no two children overlap.
    public void repositionChild(WordGroup child, boolean relativeToGroup) {
        if (!relativeToGroup && child.center.x == 0 && child.center.y == 0) {
            // Safety check: do not set relativeToGroup=false if positioning for the first time!
            // Initial placement will be messed up and will likely show up in the upper-left corner.
            relativeToGroup = true;
        }

        final double ARC_LENGTH = 20; // Length between each potential word position
        final double RADIUS_RATE = 5;  // Rate of radius growth per radian

        final int CENTER_X = relativeToGroup ? center.x : child.center.x;
        final int CENTER_Y = relativeToGroup ? center.y : child.center.y;

        double angle = ARC_LENGTH / (RADIUS_RATE * 2);

        int x = CENTER_X;
        int y = CENTER_Y;

        while (isChildOverlap(child, x, y)) {
            double radius = RADIUS_RATE * angle; // Radius grows as angle increases

            x = CENTER_X + (int)(radius * Math.cos(angle));
            y = CENTER_Y + (int)(radius * Math.sin(angle));

            if (relativeToGroup) {
                // If positioning in group, add a random x offset
                x += new Random().nextInt(bounds.width() / 2 + 1) - bounds.width() / 4;
            }

            // Estimate delta angle using ARC_LENGTH / radius
            // This will give us an angle that will result in a position ARC_LENGTH away.
            // (arc = theta * radius) -> (theta = arc / radius)
            angle += ARC_LENGTH / radius;
        }

        child.moveTo(x, y);

        // Update bounds
        bounds.setEmpty();

        for (WordGroup group : children) {
            bounds.union(group.bounds);
        }
    }

    // Checks if a group is overlapping any other groups at the specified location.
    private boolean isChildOverlap(WordGroup group, int x, int y) {
        int dx = x - group.center.x;
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
