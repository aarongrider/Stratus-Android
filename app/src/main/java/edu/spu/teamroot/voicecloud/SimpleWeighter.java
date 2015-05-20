package edu.spu.teamroot.voicecloud;

public class SimpleWeighter implements WordWeighter {
    private static final int[] accentColors = {
            R.color.accentBlue,
            R.color.accentBlue,
            R.color.accentBlueDark,
            R.color.accentPurple,
            R.color.accentPurpleDark,
            R.color.accentGreen,
            R.color.accentGreenDark,
            R.color.accentYellow,
            R.color.accentYellowDark,
            R.color.accentRed,
            R.color.accentRedDark};

    @Override
    public int getTextSize(Word word) {
        // Minimum size of word is 20
        return 20 + word.getCount();
    }

    @Override
    public int getWordColor(Word word) {
        // Change color every 5 occurrences
        int index = Math.max((word.getCount() / 1) % accentColors.length, 0);
        return WordCloud.context.getResources().getColor(accentColors[index]);
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
