package edu.spu.teamroot.voicecloud;

public class SimpleWeighter implements WordWeighter {
    private static final int[] accentColors = {
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
            R.color.red,
            R.color.redDark};

    @Override
    public int getTextSize(Word word) {
        // Minimum size of word is 20 + our weighting
        return (int) (20 + (Math.log10(word.getCount()) * 80));
    }

    @Override
    public int getWordColor(Word word) {
        // Change color every occurrence
        int index = Math.max(word.getCount() % accentColors.length, 0);
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
