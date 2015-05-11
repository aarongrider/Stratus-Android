package edu.spu.teamroot.voicecloud;

public class SimpleWeighter implements WordWeighter {
    private static final int[] accentColors = {
            R.color.accentBlue,
            R.color.accentGreen,
            R.color.accentYellow,
            R.color.accentRed};

    @Override
    public int getTextSize(Word word) {
        // Minimum size of word is 20
        return 20 + word.getCount();
    }

    @Override
    public int getWordColor(Word word) {
        // Change color every 5 occurrences
        return WordCloud.context.getResources().getColor(accentColors[(word.getCount() / 5) % accentColors.length]);
    }

    @Override
    public boolean shouldShow(Word word) {
        // Show all words greater than 1
        return word.getCount() >= 1;
    }

    @Override
    public boolean refreshAll(Word word) {
        return false;
    }
}
