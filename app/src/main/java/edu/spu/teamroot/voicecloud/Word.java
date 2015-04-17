package edu.spu.teamroot.voicecloud;

import android.widget.Button;
import android.widget.RelativeLayout;

public class Word extends WordGroup {
    // The layout containing all word views
    private static RelativeLayout layout;

    private String name;
    private int count;

    public Button wordObject;

    public Word(RelativeLayout rl) {
        super();

        if (layout == null) {
            layout = rl;
        }
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
        }
    }

    private void refreshSize() {

    }

    @Override
    public void moveTo(int x, int y) {
        super.moveTo(x, y);
        // In addition, move the actual word button!
    }
}
