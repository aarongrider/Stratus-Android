package edu.spu.teamroot.voicecloud;

public class RelativeWeighter implements WordWeighter {
    private static final int[] accentColors = {
            R.color.accentBlue,
            R.color.accentGreen,
            R.color.accentYellow,
            R.color.accentRed};

    private static final int WINDOW = 10;

    int maxCount;
    boolean refreshAll;

    public RelativeWeighter() {
        maxCount = Integer.MIN_VALUE;
    }

    @Override
    public int getTextSize(Word word) {
        int count = word.getCount();
        if (count > maxCount) {
            maxCount = count;
            refreshAll = true; // maxCount changed, refresh all!
        }

        return 20 + count;
    }

    @Override
    public int getWordColor(Word word) {
        // Change color every 5 occurrences
        return accentColors[(word.getCount() / 5) % accentColors.length];
    }

    @Override
    public boolean shouldShow(Word word) {
        // Show all words greater than WINDOW less than maxCount
        int count = word.getCount();
        return (count > Math.max(maxCount - WINDOW, 0));
    }

    @Override
    public boolean refreshAll(Word word) {
        boolean refresh = refreshAll;
        refreshAll = false;
        return refresh;
    }
}
