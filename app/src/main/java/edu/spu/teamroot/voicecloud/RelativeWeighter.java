package edu.spu.teamroot.voicecloud;

public class RelativeWeighter implements WordWeighter {
    private static final int[] accentColors = {
            R.color.blue,
            R.color.blue,
            R.color.cyan,
            R.color.teal,
            R.color.green,
            R.color.greenLight,
            R.color.lime,
            R.color.yellow,
            R.color.amber,
            R.color.orange,
            R.color.orangeDark,
            R.color.red};

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
