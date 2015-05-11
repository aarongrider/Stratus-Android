package edu.spu.teamroot.voicecloud;

public interface WordWeighter {
    // Returns the text size for a button, in sp.
    int getTextSize(Word word);

    // Returns the ARGB color for a word.
    int getWordColor(Word word);

    // Returns true if the word should be shown in the cloud, otherwise false.
    boolean shouldShow(Word word);

    // Returns true if all words need to be re-evaluated for shouldShow qualification.
    boolean refreshAll(Word word);
}
